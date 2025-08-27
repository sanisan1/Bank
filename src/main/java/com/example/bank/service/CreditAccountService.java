package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.mapper.CreditAccountMapper;
import com.example.bank.model.Account.CreditAccount;
import com.example.bank.model.Account.CreditAccountDto;
import com.example.bank.model.Account.DebitAccount.Account;
import com.example.bank.model.Account.DebitAccount.AccountDto;
import com.example.bank.model.OperationType;
import com.example.bank.model.Transaction.Transfers;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CreditAccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CreditAccountService extends AbstractAccountService {

    private final CreditAccountRepository creditAccountRepository;
    private final TransactionRepository transactionRepository;

    public CreditAccountService(AccountRepository accountRepository,
                                UserRepository userRepository,
                                AccountSecurity accountSecurity,
                                CreditAccountRepository creditAccountRepository,
                                TransactionRepository transactionRepository) {
        super(accountRepository, userRepository, accountSecurity);
        this.creditAccountRepository = creditAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    /* ----------------------- Плановое начисление процентов ----------------------- */

    /**
     * Ежемесячное начисление процентов на тело долга.
     * Выполняется автоматически 1-го числа каждого месяца.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void accrueMonthlyInterest() {
        List<CreditAccount> accounts = creditAccountRepository.findAll();
        for (CreditAccount acc : accounts) {
            acc.updateDebt();       // обновление debt и totalDebt
            acc.accrueInterest();   // начисление процентов
        }
        creditAccountRepository.saveAll(accounts);
    }

    /* ----------------------- CRUD / операции ----------------------- */

    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccountDto createAccount(BigDecimal creditLimit, BigDecimal interestRate, Integer GracePeriod) {
        var user = getCurrentUser();
        checkUserBlock(user);

        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Credit limit must be > 0");
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidOperationException("Interest rate cannot be negative");

        CreditAccount acc = new CreditAccount();
        acc.setUser(user);
        acc.setAccountNumber(generateUniqueAccountNumber());
        acc.setCreditLimit(creditLimit);
        acc.setBalance(creditLimit);
        acc.setInterestRate(interestRate);
        acc.setMinimumPaymentRate(BigDecimal.valueOf(5));
        acc.setGracePeriod(GracePeriod);
        acc.setAccruedInterest(BigDecimal.ZERO);
        acc.setDebt(BigDecimal.ZERO);
        acc.setTotalDebt(BigDecimal.ZERO);
        acc.setPaymentDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        CreditAccount saved = creditAccountRepository.save(acc);
        return CreditAccountMapper.toDto(saved);
    }

    public CreditAccount getCreditAccountById(Long accountId) {
        Account base = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditAccount", "id", accountId));
        if (!(base instanceof CreditAccount)) {
            throw new ResourceNotFoundException("CreditAccount", "id", accountId);
        }
        return (CreditAccount) base;
    }

    public CreditAccount getByNumber(String accountNumber) {
        return creditAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CreditAccount", "accountNumber", accountNumber));
    }

    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public AccountDto deposit(String accountNumber, BigDecimal amount) {
        CreditAccount acc = getByNumber(accountNumber);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");
        checkAccountBlock(acc);

        // 1) Гашение процентов
        BigDecimal toInterest = acc.getAccruedInterest().min(amount);
        acc.setAccruedInterest(acc.getAccruedInterest().subtract(toInterest));
        BigDecimal left = amount.subtract(toInterest);

        // 2) Увеличение баланса до creditLimit
        if (left.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxIncrease = acc.getCreditLimit().subtract(acc.getBalance());
            BigDecimal inc = left.min(maxIncrease);
            acc.setBalance(acc.getBalance().add(inc));
            left = left.subtract(inc);
        }

        if (left.compareTo(BigDecimal.ZERO) > 0)
            throw new InvalidOperationException("Payment exceeds required amount");

        acc.updateDebt();
        creditAccountRepository.save(acc);

        Transfers t = new Transfers();
        t.setAmount(amount);
        t.setToAccount(acc);
        t.setToUser(acc.getUser());
        t.setOperationType(OperationType.deposit);
        transactionRepository.save(t);

        return AccountMapper.toDto(acc);
    }

    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public AccountDto withdraw(String accountNumber, BigDecimal amount) {
        CreditAccount acc = getByNumber(accountNumber);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");
        checkAccountBlock(acc);

        BigDecimal available = acc.getBalance();
        if (amount.compareTo(available) > 0)
            throw new InvalidOperationException("Exceeds available credit");

        acc.setBalance(acc.getBalance().subtract(amount));
        acc.updateDebt();
        creditAccountRepository.save(acc);

        Transfers t = new Transfers();
        t.setAmount(amount);
        t.setFromAccount(acc);
        t.setFromUser(acc.getUser());
        t.setOperationType(OperationType.withdraw);
        transactionRepository.save(t);

        return AccountMapper.toDto(acc);
    }

    @Transactional
    public Transfers transfer(String fromAccNumber, String toAccNumber, BigDecimal amount, String comment) {
        CreditAccount from = getByNumber(fromAccNumber);
        Account to = getAccountByNumber(toAccNumber);

        if (!accountSecurity.isOwner(from.getId()))
            throw new InvalidOperationException("Not owner account");
        checkAccountBlock(from);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");

        BigDecimal available = from.getBalance();
        if (amount.compareTo(available) > 0)
            throw new InvalidOperationException("Exceeds available credit");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        from.updateDebt();

        accountRepository.save(from);
        accountRepository.save(to);

        Transfers t = new Transfers();
        t.setAmount(amount);
        t.setComment(comment);
        t.setFromAccount(from);
        t.setToAccount(to);
        t.setFromUser(from.getUser());
        t.setToUser(to.getUser());
        t.setOperationType(OperationType.transfer);
        transactionRepository.save(t);

        return t;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteByAccountNumber(String accountNumber) {
        CreditAccount acc = getByNumber(accountNumber);
        acc.updateDebt();
        if (acc.getTotalDebt().compareTo(BigDecimal.ZERO) != 0)
            throw new InvalidOperationException("Cannot delete credit account with outstanding debt");

        accountRepository.deleteByAccountNumber(accountNumber);
    }

    /* ----------------------- Админские методы ----------------------- */

    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount increaseCreditLimit(Long accountId, BigDecimal newLimit) {
        CreditAccount acc = getCreditAccountById(accountId);
        if (newLimit == null || newLimit.compareTo(acc.getCreditLimit()) <= 0)
            throw new InvalidOperationException("New limit must be greater than current limit");

        BigDecimal delta = newLimit.subtract(acc.getCreditLimit());
        acc.setCreditLimit(newLimit);
        acc.setBalance(acc.getBalance().add(delta));
        acc.updateDebt();

        return creditAccountRepository.save(acc);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount decreaseCreditLimit(Long accountId, BigDecimal newLimit) {
        CreditAccount acc = getCreditAccountById(accountId);
        if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("New limit must be > 0");
        if (newLimit.compareTo(acc.getBalance()) < 0)
            throw new InvalidOperationException("New limit cannot be less than current available balance");

        acc.setCreditLimit(newLimit);
        acc.updateDebt();
        return creditAccountRepository.save(acc);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount setInterestRate(Long accountId, BigDecimal newRate) {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidOperationException("Interest rate cannot be negative");

        CreditAccount acc = getCreditAccountById(accountId);
        acc.setInterestRate(newRate);
        return creditAccountRepository.save(acc);
    }
}
