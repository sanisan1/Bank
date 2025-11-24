package com.example.bank.service;

import com.example.bank.Enums.OperationType;
import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.kafka.TransactionEventProducer;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.mapper.NotificationMapper;
import com.example.bank.mapper.TransactionMapper;
import com.example.bank.model.account.Account;
import com.example.bank.model.account.AccountDto;
import com.example.bank.model.account.creditAccount.CreditAccount;
import com.example.bank.model.account.debitAccount.DebitAccount;
import com.example.bank.model.transaction.Transaction;
import com.example.bank.model.transaction.TransactionResponse;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);


    private final TransactionEventProducer eventProducer;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Map<Class<? extends Account>, AbstractAccountService> serviceMap;


    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CreditAccountService creditAccountService,
            DebitAccountService debitAccountService,
            TransactionEventProducer eventProducer   // <---- обязательно!
          ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.eventProducer = eventProducer;
        this.serviceMap = new HashMap<>();
        this.serviceMap.put(CreditAccount.class, creditAccountService);
        this.serviceMap.put(DebitAccount.class, debitAccountService);
    }


    private Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found", "AccountNumber", accountNumber));
    }

    // Операция пополнения счета
    @Transactional
    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public AccountDto deposit(String accountNumber, BigDecimal amount, String comment) {
        log.info("Deposit to account {}: amount {}", accountNumber, amount);
        try {
            Account acc = getAccountByNumber(accountNumber);
            AbstractAccountService service = serviceMap.get(acc.getClass());
            if (service == null) {
                log.error("Unsupported account type for deposit: {}", acc.getClass());
                throw new InvalidOperationException("Unsupported account type");
            }

            Account updatedAccount = service.processDeposit(acc, amount);

            Transaction transaction = new Transaction();
            transaction.setToAccount(accountNumber);
            transaction.setAmount(amount);
            transaction.setType(OperationType.deposit);
            transaction.setComment(comment);
            transaction.setUser(acc.getUser());
            transactionRepository.save(transaction);
            eventProducer.sendTransactionEvent(NotificationMapper.toEventDTO(transaction));


            return AccountMapper.toDto(updatedAccount);
        } catch (Exception e) {
            log.error("Deposit error for account {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Операция снятия со счета
    @Transactional
    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public AccountDto withdraw(String accountNumber, BigDecimal amount, String comment) {
        log.info("Withdraw from account {}: amount {}", accountNumber, amount);
        try {
            Account acc = getAccountByNumber(accountNumber);
            AbstractAccountService service = serviceMap.get(acc.getClass());
            if (service == null) {
                log.error("Unsupported account type for withdrawal: {}", acc.getClass());
                throw new InvalidOperationException("Unsupported account type");
            }

            Account updatedAccount = service.processWithdraw(acc, amount);

            Transaction transaction = new Transaction();
            transaction.setFromAccount(acc.getAccountNumber());
            transaction.setAmount(amount);
            transaction.setType(OperationType.withdraw);
            transaction.setComment(comment);
            transaction.setUser(acc.getUser());
            transactionRepository.save(transaction);


            eventProducer.sendTransactionEvent(NotificationMapper.toEventDTO(transaction));


            return AccountMapper.toDto(updatedAccount);
        } catch (Exception e) {
            log.error("Withdraw error for account {}: {}", accountNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Операция перевода средств
    @Transactional
    @PreAuthorize("@accountSecurity.isOwner(#fromNumber)")
    public AccountDto transfer(String fromNumber, String toNumber, BigDecimal amount, String comment) {
        log.info("Transfer: {} → {}, amount {}", fromNumber, toNumber, amount);
        try {
            Account fromAcc = getAccountByNumber(fromNumber);
            Account toAcc = getAccountByNumber(toNumber);

            AbstractAccountService fromService = serviceMap.get(fromAcc.getClass());
            AbstractAccountService toService = serviceMap.get(toAcc.getClass());
            if (fromService == null || toService == null) {
                log.error("Unsupported account type for transfer: {} or {}", fromAcc.getClass(), toAcc.getClass());
                throw new InvalidOperationException("Unsupported account type");
            }

            fromAcc = fromService.processWithdraw(fromAcc, amount);
            toService.processDeposit(toAcc, amount);

            Transaction transaction = new Transaction();
            transaction.setFromAccount(fromNumber);
            transaction.setToAccount(toNumber);
            transaction.setAmount(amount);
            transaction.setType(OperationType.transfer);
            transaction.setComment(comment);
            transaction.setUser(fromAcc.getUser());
            transactionRepository.save(transaction);

            eventProducer.sendTransactionEvent(NotificationMapper.toEventDTO(transaction));


            return AccountMapper.toDto(fromAcc);

        } catch (Exception e) {
            log.error("Transfer error from {} to {}: {}", fromNumber, toNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Получить транзакцию по ID
    public TransactionResponse getTransactionById(Long id) {
        return TransactionMapper.toDto(
                transactionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id))
        );
    }

    // Получить все транзакции
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Получить транзакции по счету
    @PreAuthorize("@accountSecurity.isOwner(#accountNumber)")
    public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByFromAccountOrToAccount(accountNumber, accountNumber);
        return transactions.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Получить транзакции по пользователю
    @PreAuthorize("@accountSecurity.isSelfOrAdmin(#userId)")
    public List<TransactionResponse> getTransactionsByUser(Long userId) {
        List<Account> userAccounts = accountRepository.findByUserUserId(userId);
        List<String> accountNumbers = userAccounts.stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toList());

        if (accountNumbers.isEmpty()) {
            return List.of();
        }

        List<Transaction> transactions = transactionRepository.findByFromAccountInOrToAccountIn(
                accountNumbers,
                accountNumbers
        );

        return transactions.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }
}