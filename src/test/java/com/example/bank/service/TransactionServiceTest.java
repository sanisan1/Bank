package com.example.bank.service;

import com.example.bank.Enums.OperationType;
import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.kafka.TransactionEventProducer;
import com.example.bank.model.account.AccountDto;
import com.example.bank.model.account.creditAccount.CreditAccount;
import com.example.bank.model.account.debitAccount.DebitAccount;
import com.example.bank.model.transaction.Transaction;
import com.example.bank.model.transaction.TransactionResponse;
import com.example.bank.model.user.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CreditAccountService creditAccountService;
    @Mock private DebitAccountService debitAccountService;
    @Mock private TransactionEventProducer eventProducer;

    @InjectMocks
    private TransactionService transactionService;

    private CreditAccount creditAccount;
    private DebitAccount debitAccount;

    @BeforeEach
    void setUp() {
        creditAccount = new CreditAccount();
        creditAccount.setAccountNumber("CR123");
        creditAccount.setBalance(BigDecimal.valueOf(1000));

        debitAccount = new DebitAccount();
        debitAccount.setAccountNumber("DB123");
        debitAccount.setBalance(BigDecimal.valueOf(500));
    }

    @Test
    void deposit_ShouldWorkForCreditAccount() {
        BigDecimal amount = BigDecimal.valueOf(100);
        CreditAccount updated = new CreditAccount();
        updated.setAccountNumber("CR123");
        updated.setBalance(BigDecimal.valueOf(1100));
        // И user!
        User user = new User();
        user.setUserId(123L);
        creditAccount.setUser(user);
        updated.setUser(user); // и для updated, чтобы AccountMapper не упал

        when(accountRepository.findByAccountNumber("CR123")).thenReturn(Optional.of(creditAccount));
        when(creditAccountService.processDeposit(eq(creditAccount), eq(amount))).thenReturn(updated);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = transactionService.deposit("CR123", amount, "ok");
        assertEquals(updated.getBalance(), result.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
        verify(eventProducer).sendTransactionEvent(any());
    }


    @Test
    void deposit_ShouldThrowForNegativeAmount_Credit() {
        BigDecimal negative = BigDecimal.valueOf(-100);
        when(accountRepository.findByAccountNumber("CR123")).thenReturn(Optional.of(creditAccount));
        when(creditAccountService.processDeposit(eq(creditAccount), eq(negative)))
                .thenThrow(new InvalidOperationException("Amount must be greater than zero"));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> transactionService.deposit("CR123", negative, "invalid"));
        assertEquals("Amount must be greater than zero", ex.getMessage());
        verify(transactionRepository, never()).save(any());
        verify(eventProducer, never()).sendTransactionEvent(any());
    }

    @Test
    void deposit_ShouldThrowIfAccountNotFound() {
        when(accountRepository.findByAccountNumber("XXX")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deposit("XXX", BigDecimal.TEN, "no"));
        verify(transactionRepository, never()).save(any());
        verify(eventProducer, never()).sendTransactionEvent(any());
    }

    @Test
    void withdraw_ShouldWorkForDebitAccount() {
        BigDecimal amount = BigDecimal.valueOf(50);
        DebitAccount updated = new DebitAccount();
        updated.setAccountNumber("DB123");
        updated.setBalance(BigDecimal.valueOf(450));
        User user = new User();
        user.setUserId(12L);
        updated.setUser(user);
        // ВАЖНО! Вот эта строка нужна:
        debitAccount.setUser(user);

        when(accountRepository.findByAccountNumber("DB123")).thenReturn(Optional.of(debitAccount));
        when(debitAccountService.processWithdraw(eq(debitAccount), eq(amount))).thenReturn(updated);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = transactionService.withdraw("DB123", amount, "ok");
        assertEquals(updated.getBalance(), result.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
        verify(eventProducer).sendTransactionEvent(any());
    }


    @Test
    void withdraw_ShouldThrowForNegativeAmount_Debit() {
        BigDecimal negative = BigDecimal.valueOf(-100);
        when(accountRepository.findByAccountNumber("DB123")).thenReturn(Optional.of(debitAccount));
        when(debitAccountService.processWithdraw(eq(debitAccount), eq(negative)))
                .thenThrow(new InvalidOperationException("Amount must be greater than zero"));

        assertThrows(InvalidOperationException.class,
                () -> transactionService.withdraw("DB123", negative, "invalid"));
        verify(transactionRepository, never()).save(any());
        verify(eventProducer, never()).sendTransactionEvent(any());
    }

    @Test
    void transfer_ShouldWork_FromDebitToCredit() {
        BigDecimal amount = BigDecimal.valueOf(100);

        DebitAccount from = new DebitAccount();
        from.setAccountNumber("DB12");
        from.setBalance(BigDecimal.valueOf(600));
        User user = new User();
        user.setUserId(12L);
        from.setUser(user);

        CreditAccount to = new CreditAccount();
        to.setAccountNumber("CR1");
        to.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findByAccountNumber("DB12")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("CR1")).thenReturn(Optional.of(to));
        when(debitAccountService.processWithdraw(eq(from), eq(amount))).thenReturn(from);
        when(creditAccountService.processDeposit(eq(to), eq(amount))).thenReturn(to);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = transactionService.transfer("DB12", "CR1", amount, "ok");

        assertEquals("DB12", result.getAccountNumber());
        verify(transactionRepository).save(any(Transaction.class));
        verify(eventProducer).sendTransactionEvent(any());
    }

    @Test
    void transfer_ShouldThrowForNegativeAmount() {
        BigDecimal negative = BigDecimal.valueOf(-100);
        DebitAccount from = new DebitAccount(); from.setAccountNumber("DB1");
        CreditAccount to = new CreditAccount(); to.setAccountNumber("CR1");

        when(accountRepository.findByAccountNumber("DB1")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("CR1")).thenReturn(Optional.of(to));
        when(debitAccountService.processWithdraw(eq(from), eq(negative)))
                .thenThrow(new InvalidOperationException("Amount must be greater than zero"));

        assertThrows(InvalidOperationException.class,
                () -> transactionService.transfer("DB1", "CR1", negative, "fail"));
        verify(transactionRepository, never()).save(any());
        verify(eventProducer, never()).sendTransactionEvent(any());
    }

    @Test
    void getTransactionById_ShouldReturnDto() {
        Transaction t = new Transaction();
        t.setAmount(BigDecimal.TEN);
        t.setType(OperationType.deposit);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));

        TransactionResponse dto = transactionService.getTransactionById(1L);
        assertEquals(BigDecimal.TEN, dto.getAmount());
    }

    @Test
    void getTransactionById_ShouldThrowIfNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(99L));
    }

    @Test
    void getAllTransactions_ShouldReturnList() {
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TransactionResponse> result = transactionService.getAllTransactions();
        assertEquals(2, result.size());
    }

    @Test
    void getTransactionsByAccount_ShouldReturnFilteredList() {
        Transaction t = new Transaction();
        t.setFromAccount("DB123");
        t.setAmount(BigDecimal.TEN);
        when(transactionRepository.findByFromAccountOrToAccount("DB123", "DB123"))
                .thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactionsByAccount("DB123");
        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByUser_ShouldReturnCombinedList() {
        // Реализуй аналогично — только моки в самом тесте!
    }

    @Test
    void getTransactionsByUser_ShouldReturnEmptyIfNoAccounts() {
        when(accountRepository.findByUserUserId(2L)).thenReturn(List.of());
        List<TransactionResponse> result = transactionService.getTransactionsByUser(2L);
        assertTrue(result.isEmpty());
        verify(transactionRepository, never()).findByFromAccountInOrToAccountIn(anyList(), anyList());
    }
}
