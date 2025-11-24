package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.CreditAccountMapper;
import com.example.bank.model.account.creditAccount.CreditAccount;
import com.example.bank.model.account.creditAccount.CreditAccountResponseDto;
import com.example.bank.model.account.Account;
import com.example.bank.Enums.AccountType;
import com.example.bank.model.user.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CreditAccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Сервис для работы с кредитными счетами
@Service
public class CreditAccountService extends AbstractAccountService {
    // Базовые значения кредита
    @Value("${bank.credit.default.interest-rate}")
    private BigDecimal defaultInterestRate;

    @Value("${bank.credit.default.limit}")
    private BigDecimal defaultCreditLimit;

    @Value("${bank.credit.default.minimum-payment-rate}")
    private BigDecimal defaultMinimumPaymentRate;

    @Value("${bank.credit.default.grace-period}")
    private Integer defaultGracePeriod;


    private static final Logger log = LoggerFactory.getLogger(CreditAccountService.class);

    private final CreditAccountRepository creditAccountRepository;
    private final TransactionRepository transactionRepository;

    // Конструктор сервиса кредитных счетов
    public CreditAccountService(AccountRepository accountRepository,
                                UserRepository userRepository,
                                AccountSecurity accountSecurity,
                                CreditAccountRepository creditAccountRepository,
                                TransactionRepository transactionRepository) {
        super(accountRepository, userRepository, accountSecurity);
        this.creditAccountRepository = creditAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Начисление ежемесячных процентов по всем кредитным счетам
    @Scheduled(cron = "0 0 0 1 * ?")
    public void accrueMonthlyInterest() {
        log.info("Monthly interest accrual started for all credit accounts");
        Pageable pageable = PageRequest.of(0, 500);
        Page<CreditAccount> accountPage;
        do {
            accountPage = creditAccountRepository.findAll(pageable);
            accountPage.getContent().forEach(acc -> {
                try {
                    acc.accrueInterest();
                    acc.updateTotalDebt();
                    creditAccountRepository.save(acc);
                } catch (Exception e) {
                    log.error("Error accruing interest for account {}: {}", acc.getAccountNumber(), e.getMessage(), e);
                }
            });
            pageable = accountPage.nextPageable();
        } while (accountPage.hasNext());
        log.info("Monthly interest accrual finished for all credit accounts");
    }

    // Создание кредитного счета администратором
    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccountResponseDto createAccount(Long userID, BigDecimal creditLimit, BigDecimal interestRate, Integer gracePeriod) {
        log.info("Creating credit account for userID={} with limit {} and rate {}", userID, creditLimit, interestRate);
        try {
            User user = userRepository.findById(userID)
                    .orElseThrow(() -> {
                        log.error("User not found with id={}", userID);
                        return new ResourceNotFoundException("User", "id", userID);
                    });

            if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid credit limit: {}", creditLimit);
                throw new InvalidOperationException("Credit limit must be > 0");
            }
            if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Invalid interest rate: {}", interestRate);
                throw new InvalidOperationException("Interest rate cannot be negative");
            }

            CreditAccount acc = new CreditAccount();
            acc.setUser(user);
            acc.setAccountNumber(generateUniqueAccountNumber());
            acc.setCreditLimit(creditLimit);
            acc.setBalance(acc.getCreditLimit());
            acc.setInterestRate(interestRate);
            acc.setMinimumPaymentRate(BigDecimal.valueOf(5)); // Значение по умолчанию
            acc.setGracePeriod(gracePeriod);
            acc.setAccruedInterest(BigDecimal.ZERO);
            acc.setAccountType(AccountType.CREDIT);

            CreditAccount saved = creditAccountRepository.save(acc);
            log.info("Credit account {} created for user {}", saved.getAccountNumber(), userID);
            return CreditAccountMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error creating credit account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Создание кредитного счета обычным пользователем
    public CreditAccountResponseDto createAccountforUser() {
        User user = getCurrentUser();
        Long userId = user.getUserId();
        log.info("Creating credit account by user{} with limit {} and rate {}", userId, defaultCreditLimit, defaultInterestRate);
        try {

            if (!creditAccountRepository.findByUserUserId(userId).isEmpty()) {
                throw new RuntimeException("User can make only 1 credit account by himself");
            }

            CreditAccount acc = new CreditAccount();
            acc.setUser(user);
            acc.setAccountNumber(generateUniqueAccountNumber());
            acc.setCreditLimit(defaultCreditLimit);
            acc.setBalance(acc.getCreditLimit());
            acc.setInterestRate(defaultInterestRate);
            acc.setMinimumPaymentRate(defaultMinimumPaymentRate);
            acc.setGracePeriod(defaultGracePeriod);
            acc.setAccruedInterest(BigDecimal.ZERO);
            acc.setAccountType(AccountType.CREDIT);

            CreditAccount saved = creditAccountRepository.save(acc);
            log.info("Credit account {} created for user {}", saved.getAccountNumber(), userId);
            return CreditAccountMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error creating credit account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Обработка операции пополнения кредитного счета
    @Override
    public Account processDeposit(Account account, BigDecimal amount) {
        log.info("Depositing to credit account {} amount {}", account.getAccountNumber(), amount);
        try {
            CreditAccount acc = (CreditAccount) account;
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Deposit attempt with invalid amount: {}", amount);
                throw new InvalidOperationException("Amount must be greater than zero");
            }
            checkAccountBlock(acc);

            BigDecimal toInterest = acc.getAccruedInterest().min(amount);
            acc.setAccruedInterest(acc.getAccruedInterest().subtract(toInterest));
            BigDecimal left = amount.subtract(toInterest);

            if (left.compareTo(BigDecimal.ZERO) > 0) {
                acc.setBalance(acc.getBalance().add(left));
                acc.setDebt(acc.getDebt().subtract(left));
            }
            acc.updateTotalDebt();
            return acc;
        } catch (Exception e) {
            log.error("Error depositing to credit account {}: {}", account.getAccountNumber(), e.getMessage(), e);
            throw e;
        }
    }

    // Обработка операции снятия с кредитного счета
    @Override
    public Account processWithdraw(Account account, BigDecimal amount) {
        log.info("Withdrawing from credit account {} amount {}", account.getAccountNumber(), amount);
        try {
            CreditAccount acc = (CreditAccount) account;
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Withdraw attempt with invalid amount: {}", amount);
                throw new InvalidOperationException("Amount must be greater than zero");
            }
            checkAccountBlock(acc);

            BigDecimal balance = account.getBalance();
            if (amount.compareTo(balance) > 0) {
                log.error("Insufficient funds: account {}, balance {}, withdrawal attempt {}", account.getAccountNumber(), balance, amount);
                throw new InvalidOperationException("Exceeds available credit");
            }
            BigDecimal creditLimit = acc.getCreditLimit();
            balance = balance.subtract(amount);

            BigDecimal totalDebt = acc.getDebt();
            BigDecimal availableOwnFunds = creditLimit.subtract(totalDebt);
            if (balance.compareTo(availableOwnFunds) < 0) {
                BigDecimal newDebt = availableOwnFunds.subtract(balance);
                totalDebt = totalDebt.add(newDebt);
            }

            acc.setBalance(balance);
            acc.setDebt(totalDebt);
            acc.updateTotalDebt();

            return acc;
        } catch (Exception e) {
            log.error("Error withdrawing from credit account {}: {}", account.getAccountNumber(), e.getMessage(), e);
            throw e;
        }
    }

    // Удаление кредитного счета по номеру (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteByAccountNumber(String accountNumber) {
        log.info("Deleting credit account: {}", accountNumber);
        try {
            CreditAccount acc = (CreditAccount) getAccountByNumber(accountNumber);
            acc.updateTotalDebt();
            if (acc.hasDebt()) {
                log.error("Delete attempt for account {} with outstanding debt", accountNumber);
                throw new InvalidOperationException("Cannot delete credit account with outstanding debt");
            }
            accountRepository.deleteByAccountNumber(accountNumber);
            log.info("Credit account {} successfully deleted", accountNumber);
        } catch (Exception e) {
            log.error("Error deleting credit account {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Увеличение кредитного лимита (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount increaseCreditLimit(String accountNumber, BigDecimal newLimit) {
        log.info("Increasing credit limit for account {} to {}", accountNumber, newLimit);
        try {
            CreditAccount acc = (CreditAccount) getAccountByNumber(accountNumber);
            if (newLimit == null || newLimit.compareTo(acc.getCreditLimit()) <= 0) {
                log.error("Invalid new limit {} (current limit = {})", newLimit, acc.getCreditLimit());
                throw new InvalidOperationException("New limit must be greater than current limit");
            }

            BigDecimal delta = newLimit.subtract(acc.getCreditLimit());
            acc.setCreditLimit(newLimit);
            acc.setBalance(acc.getBalance().add(delta));
            acc.updateTotalDebt();

            return acc;
        } catch (Exception e) {
            log.error("Error increasing limit {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Уменьшение кредитного лимита (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount decreaseCreditLimit(String accountNumber, BigDecimal newLimit) {
        log.info("Decreasing credit limit for account {} to {}", accountNumber, newLimit);
        try {
            CreditAccount acc = (CreditAccount) getAccountByNumber(accountNumber);
            if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Attempt to set invalid limit: {}", newLimit);
                throw new InvalidOperationException("New limit must be > 0");
            }
            if (newLimit.compareTo(acc.getBalance()) < 0) {
                log.error("Attempt to set limit below balance: limit={}, balance={}", newLimit, acc.getBalance());
                throw new InvalidOperationException("New limit cannot be less than current available balance");
            }

            acc.setCreditLimit(newLimit);
            acc.updateTotalDebt();
            return acc;
        } catch (Exception e) {
            log.error("Error decreasing limit {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Установка процентной ставки (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    public CreditAccount setInterestRate(String accountNumber, BigDecimal newRate) {
        log.info("Setting new interest rate for account {}: {}", accountNumber, newRate);
        try {
            if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Attempt to set negative interest rate: {}", newRate);
                throw new InvalidOperationException("Interest rate cannot be negative");
            }

            CreditAccount acc = (CreditAccount) getAccountByNumber(accountNumber);
            acc.setInterestRate(newRate);
            return acc;
        } catch (Exception e) {
            log.error("Error setting interest rate for account {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }
}