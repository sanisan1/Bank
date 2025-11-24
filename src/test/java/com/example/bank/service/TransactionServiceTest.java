package com.example.bank.service;

import com.example.bank.Enums.OperationType;
import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.mapper.TransactionMapper;
import com.example.bank.model.Account.Account;
import com.example.bank.model.Account.AccountDto;
import com.example.bank.model.Account.CreditAccount.CreditAccount;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.Transaction.Transaction;
import com.example.bank.model.Transaction.TransactionDto;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CreditAccountService creditAccountService;
    @Mock private DebitAccountService debitAccountService;

    @InjectMocks
    private TransactionService transactionService;

    private CreditAccount creditAccount;
    private DebitAccount debitAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Создаем тестовые счета
        creditAccount = new CreditAccount();
        creditAccount.setAccountNumber("CR123");
        creditAccount.setBalance(BigDecimal.valueOf(1000));
        creditAccount.setCreditLimit(BigDecimal.valueOf(500));
        creditAccount.setDebt(BigDecimal.ZERO);
        creditAccount.setAccruedInterest(BigDecimal.ZERO);

        debitAccount = new DebitAccount();
        debitAccount.setAccountNumber("DB123");
        debitAccount.setBalance(BigDecimal.valueOf(500));

        // Сохраняем транзакции как мок
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ============================================================
    // DEPOSIT TESTS
    // ============================================================

    @Test
    void deposit_ShouldWorkForCreditAccount() {
        // Проверка успешного депозита на кредитный счет
        BigDecimal amount = BigDecimal.valueOf(100);
        CreditAccount updated = new CreditAccount();
        updated.setAccountNumber("CR123");
        updated.setBalance(BigDecimal.valueOf(1100));

        when(accountRepository.findByAccountNumber("CR123")).thenReturn(Optional.of(creditAccount));
        when(creditAccountService.processDeposit(eq(creditAccount), eq(amount))).thenReturn(updated);

        AccountDto result = transactionService.deposit("CR123", amount, "ok");
        assertEquals(updated.getBalance(), result.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_ShouldThrowForNegativeAmount_Credit() {
        // Проверка депозита отрицательной суммы на кредитном счете
        BigDecimal negative = BigDecimal.valueOf(-100);
        when(accountRepository.findByAccountNumber("CR123")).thenReturn(Optional.of(creditAccount));
        when(creditAccountService.processDeposit(eq(creditAccount), eq(negative)))
                .thenThrow(new InvalidOperationException("Amount must be greater than zero"));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> transactionService.deposit("CR123", negative, "invalid"));
        assertEquals("Amount must be greater than zero", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deposit_ShouldThrowIfAccountNotFound() {
        // Депозит на несуществующий счет
        when(accountRepository.findByAccountNumber("XXX")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deposit("XXX", BigDecimal.TEN, "no"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deposit_ShouldThrowIfAccountTypeUnsupported() {
        // Депозит на неизвестный тип счета
        Account unknownType = mock(Account.class);
        when(accountRepository.findByAccountNumber("UNK")).thenReturn(Optional.of(unknownType));
        assertThrows(InvalidOperationException.class,
                () -> transactionService.deposit("UNK", BigDecimal.TEN, "invalid"));
    }

    // ============================================================
    // WITHDRAW TESTS
    // ============================================================

    @Test
    void withdraw_ShouldWorkForDebitAccount() {
        // Проверка успешного снятия с дебетового счета
        BigDecimal amount = BigDecimal.valueOf(50);
        DebitAccount updated = new DebitAccount();
        updated.setAccountNumber("DB123");
        updated.setBalance(BigDecimal.valueOf(450));

        when(accountRepository.findByAccountNumber("DB123")).thenReturn(Optional.of(debitAccount));
        when(debitAccountService.processWithdraw(eq(debitAccount), eq(amount))).thenReturn(updated);

        AccountDto result = transactionService.withdraw("DB123", amount, "ok");
        assertEquals(updated.getBalance(), result.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldThrowForNegativeAmount_Debit() {
        // Снятие отрицательной суммы с дебета
        BigDecimal negative = BigDecimal.valueOf(-100);
        when(accountRepository.findByAccountNumber("DB123")).thenReturn(Optional.of(debitAccount));
        when(debitAccountService.processWithdraw(eq(debitAccount), eq(negative)))
                .thenThrow(new InvalidOperationException("Amount must be greater than zero"));

        assertThrows(InvalidOperationException.class,
                () -> transactionService.withdraw("DB123", negative, "invalid"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_ShouldThrowIfAccountNotFound() {
        // Снятие со счета, который не найден
        when(accountRepository.findByAccountNumber("404")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.withdraw("404", BigDecimal.ONE, "none"));
    }

    @Test
    void withdraw_ShouldThrowIfUnsupportedType() {
        // Снятие с неподдерживаемого типа счета
        Account fake = mock(Account.class);
        when(accountRepository.findByAccountNumber("FAKE")).thenReturn(Optional.of(fake));
        assertThrows(InvalidOperationException.class,
                () -> transactionService.withdraw("FAKE", BigDecimal.ONE, "no"));
    }

    // ============================================================
    // TRANSFER TESTS
    // ============================================================

    @Test
    void transfer_ShouldWork_FromDebitToCredit() {
        // Успешный перевод с дебета на кредит
        BigDecimal amount = BigDecimal.valueOf(100);
        DebitAccount from = new DebitAccount(); from.setAccountNumber("DB1"); from.setBalance(BigDecimal.valueOf(600));
        CreditAccount to = new CreditAccount(); to.setAccountNumber("CR1"); to.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findByAccountNumber("DB1")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("CR1")).thenReturn(Optional.of(to));
        when(debitAccountService.processWithdraw(eq(from), eq(amount))).thenReturn(from);
        when(creditAccountService.processDeposit(eq(to), eq(amount))).thenReturn(to);

        AccountDto result = transactionService.transfer("DB1", "CR1", amount, "ok");
        assertEquals("DB1", result.getAccountNumber());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_ShouldThrowForNegativeAmount() {
        // Перевод отрицательной суммы
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
    }

    @Test
    void transfer_ShouldThrowIfUnsupportedType() {
        // Перевод между неподдерживаемыми типами счетов
        Account fakeFrom = mock(Account.class);
        Account fakeTo = mock(Account.class);
        when(accountRepository.findByAccountNumber("FROM")).thenReturn(Optional.of(fakeFrom));
        when(accountRepository.findByAccountNumber("TO")).thenReturn(Optional.of(fakeTo));

        assertThrows(InvalidOperationException.class,
                () -> transactionService.transfer("FROM", "TO", BigDecimal.TEN, "fail"));
    }

    // ============================================================
    // OTHER METHODS
    // ============================================================

    @Test
    void getTransactionById_ShouldReturnDto() {
        // Получение транзакции по ID
        Transaction t = new Transaction();
        t.setAmount(BigDecimal.TEN);
        t.setType(OperationType.deposit);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));

        TransactionDto dto = transactionService.getTransactionById(1L);
        assertEquals(BigDecimal.TEN, dto.getAmount());
    }

    @Test
    void getTransactionById_ShouldThrowIfNotFound() {
        // Получение несуществующей транзакции
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(99L));
    }

    @Test
    void getAllTransactions_ShouldReturnList() {
        // Получение всех транзакций
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TransactionDto> result = transactionService.getAllTransactions();
        assertEquals(2, result.size());
    }

    @Test
    void getTransactionsByAccount_ShouldReturnFilteredList() {
        // Получение транзакций по номеру счета
        Transaction t = new Transaction();
        t.setFromAccount("DB123");
        t.setAmount(BigDecimal.TEN);
        when(transactionRepository.findByFromAccountOrToAccount("DB123", "DB123"))
                .thenReturn(List.of(t));

        List<TransactionDto> result = transactionService.getTransactionsByAccount("DB123");
        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByUser_ShouldReturnCombinedList() {
        // Получение всех транзакций пользователя
        Account acc1 = new DebitAccount(); acc1.setAccountNumber("DB1");
        Account acc2 = new CreditAccount(); acc2.setAccountNumber("CR1");
        when(accountRepository.findByUserUserId(1L)).thenReturn(List.of(acc1, acc2));

        Transaction t1 = new Transaction(); t1.setFromAccount("DB1");
        Transaction t2 = new Transaction(); t2.setToAccount("CR1");
        when(transactionRepository.findByFromAccountInOrToAccountIn(anyList(), anyList()))
                .thenReturn(List.of(t1, t2));

        List<TransactionDto> result = transactionService.getTransactionsByUser(1L);
        assertEquals(2, result.size());
    }

    @Test
    void getTransactionsByUser_ShouldReturnEmptyIfNoAccounts() {
        // Пользователь без счетов
        when(accountRepository.findByUserUserId(2L)).thenReturn(List.of());
        List<TransactionDto> result = transactionService.getTransactionsByUser(2L);
        assertTrue(result.isEmpty());
        verify(transactionRepository, never()).findByFromAccountInOrToAccountIn(anyList(), anyList());
    }
}
