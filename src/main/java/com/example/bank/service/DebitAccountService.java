package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.mapper.DebitAccountMapper;
import com.example.bank.mapper.TransferMapper;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.Account.DebitAccount.DebitAccountResponse;
import com.example.bank.model.Transaction.TransferResponseDto;
import com.example.bank.model.Transaction.Transfers;
import com.example.bank.Enums.OperationType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DebitAccountService extends AbstractAccountService {

    private final TransactionRepository transactionRepository;

    public DebitAccountService(AccountRepository accountRepository,
                               UserRepository userRepository,
                               AccountSecurity accountSecurity,
                               TransactionRepository transactionRepository) {
        super(accountRepository, userRepository, accountSecurity);
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public DebitAccountResponse createAccount() {
        var user = getCurrentUser();
        checkUserBlock(user);

        DebitAccount account = new DebitAccount();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());

        DebitAccount saved = (DebitAccount) accountRepository.save(account);


        if (user.getMainAccount() == null) {
            user.setMainAccount(saved);
            userRepository.save(user);
        }

        return DebitAccountMapper.toDto(saved);
    }

    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public DebitAccountResponse deposit(String accountNumber, BigDecimal amount) {
        DebitAccount account = (DebitAccount) getAccountByNumber(accountNumber);

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transfers t = new Transfers();
        t.setAmount(amount);
        t.setToAccount(account);
        t.setToUser(account.getUser());
        t.setOperationType(OperationType.deposit);
        transactionRepository.save(t);

        return DebitAccountMapper.toDto(account);
    }

    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public DebitAccountResponse withdraw(String accountNumber, BigDecimal amount) {
        DebitAccount account = (DebitAccount) getAccountByNumber(accountNumber);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");

        if (amount.compareTo(account.getBalance()) > 0)
            throw new InvalidOperationException("Not enough funds");

        if (Boolean.TRUE.equals(account.getBlocked()))
            throw new InvalidOperationException("Account is blocked");

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        return DebitAccountMapper.toDto(account);
    }

    @Transactional
    public void deleteAccount(String accountNumber) {

        var user = getCurrentUser();
        DebitAccount account = (DebitAccount) getAccountByNumber(accountNumber);
        checkAccountBlock(account);
        checkUserBlock(user);
        if (user.getMainAccount().getAccountNumber().equals(accountNumber))
            throw new InvalidOperationException("Cannot delete main account");

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0)
            throw new InvalidOperationException("Cannot delete account with balance > 0");

        accountRepository.deleteByAccountNumber(accountNumber);
    }

    @Transactional
    public TransferResponseDto transfer(String fromAcc, String toAcc, BigDecimal amount, String comment) {
        var user = getCurrentUser();

        DebitAccount from = fromAcc == null ? (DebitAccount) user.getMainAccount() : (DebitAccount) getAccountByNumber(fromAcc);
        DebitAccount to = (DebitAccount) getAccountByNumber(toAcc);
        checkAccountBlock(from);
        checkUserBlock(user);

        if (!accountSecurity.isOwner(from.getId()))
            throw new InvalidOperationException("Not owner account");

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Amount must be greater than zero");

        if (Boolean.TRUE.equals(from.getBlocked()))
            throw new InvalidOperationException("Sender account is blocked");

        if (amount.compareTo(from.getBalance()) > 0)
            throw new InvalidOperationException("Not enough funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

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
        TransferResponseDto responseDto = TransferMapper.toDto(t);
        return responseDto;
    }

//    public List<DebitAccount> findByUserId(Long userId) {
//        return accountRepository.findByUserUserId(userId);
//    }
}
