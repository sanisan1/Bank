package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.model.account.creditAccount.CreditAccount;
import com.example.bank.repository.*;
import com.example.bank.security.AccountSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreditAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CreditAccountRepository creditAccountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountSecurity accountSecurity;

    @InjectMocks
    private CreditAccountService creditAccountService;

    private CreditAccount account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        account = new CreditAccount();
        account.setCreditLimit(BigDecimal.valueOf(1000));
        account.setBalance(BigDecimal.valueOf(1000));
        account.setDebt(BigDecimal.ZERO);
        account.setAccruedInterest(BigDecimal.ZERO);
        account.setInterestRate(BigDecimal.valueOf(12)); // 12% годовых
    }


    /**
     * Проверяет, что при снятии суммы в пределах кредитного лимита:
     * - баланс уменьшается на сумму;
     * - тело долга увеличивается на ту же сумму.
     */
    @Test
    void testProcessWithdrawWithinLimit() {
        creditAccountService.processWithdraw(account, BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(800), account.getBalance());
        assertEquals(BigDecimal.valueOf(200), account.getDebt());
    }

    /**
     * Проверяет, что при попытке снять больше, чем доступно по лимиту,
     * выбрасывается исключение InvalidOperationException.
     */
    @Test
    void testProcessWithdrawExceedsLimitThrows() {
        assertThrows(InvalidOperationException.class, () ->
                creditAccountService.processWithdraw(account, BigDecimal.valueOf(2000)));
    }

    /**
     * Проверяет, что при внесении денег на счёт без процентов:
     * - долг уменьшается на сумму пополнения;
     * - баланс увеличивается.
     */
    @Test
    void testProcessDepositReducesDebt() {
        account.setDebt(BigDecimal.valueOf(300));
        account.setBalance(BigDecimal.valueOf(700));

        creditAccountService.processDeposit(account, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(800), account.getBalance());
        assertEquals(BigDecimal.valueOf(200), account.getDebt());
    }

    /**
     * Проверяет начисление процентов на тело долга.
     * Процент начисляется только на Debt, не на проценты.
     */
    @Test
    void testAccrueMonthlyInterestAddsInterest() {
        account.setDebt(BigDecimal.valueOf(1000));

        Page<CreditAccount> page = new PageImpl<>(List.of(account));
        when(creditAccountRepository.findAll(any(Pageable.class))).thenReturn(page);

        creditAccountService.accrueMonthlyInterest();

        // 12% годовых = 1% в месяц → 10 от 1000
        assertEquals(BigDecimal.valueOf(10.00).setScale(2), account.getAccruedInterest().setScale(2));
        verify(creditAccountRepository, atLeastOnce()).save(account);
    }

    /**
     * Проверяет, что при внесении суммы, большей чем проценты:
     * - проценты гасятся полностью;
     * - остаток идёт в погашение тела;
     * - баланс растёт на ту же величину, что уменьшился долг.
     */
    @Test
    void testDepositPaysOffInterestAndPartOfDebt() {
        account.setTotalDebt(BigDecimal.valueOf(550));   // 500 тело + 50 проценты
        account.setDebt(BigDecimal.valueOf(500));
        account.setAccruedInterest(BigDecimal.valueOf(50));
        account.setBalance(BigDecimal.valueOf(500));

        creditAccountService.processDeposit(account, BigDecimal.valueOf(60));

        assertEquals(BigDecimal.ZERO, account.getAccruedInterest());   // проценты погашены
        assertEquals(BigDecimal.valueOf(490), account.getDebt());      // долг уменьшился на 10
        assertEquals(BigDecimal.valueOf(510), account.getBalance());   // баланс вырос на 10
        assertEquals(BigDecimal.valueOf(490), account.getTotalDebt()); // общий долг пересчитан
    }

    /**
     * Проверяет, что при внесении суммы, меньшей чем проценты:
     * - тело долга не уменьшается;
     * - проценты уменьшаются на внесённую сумму;
     * - баланс не изменяется.
     */
    @Test
    void testDepositPaysOffPartOfInterestOnly() {
        account.setTotalDebt(BigDecimal.valueOf(550));   // 500 тело + 50 проценты
        account.setDebt(BigDecimal.valueOf(500));
        account.setAccruedInterest(BigDecimal.valueOf(50));
        account.setBalance(BigDecimal.valueOf(500));

        creditAccountService.processDeposit(account, BigDecimal.valueOf(40));

        assertEquals(BigDecimal.valueOf(10), account.getAccruedInterest());
        assertEquals(BigDecimal.valueOf(500), account.getDebt());
        assertEquals(BigDecimal.valueOf(500), account.getBalance());
        assertEquals(BigDecimal.valueOf(510), account.getTotalDebt());
    }

    /**
     * Проверяет, что при снятии 300 со счёта с балансом 1200
     * тело долга корректно обновляется.
     */
    @Test
    void testWithdrawCreatesDebtCorrectly() {
        account.setBalance(BigDecimal.valueOf(1200));

        creditAccountService.processWithdraw(account, BigDecimal.valueOf(300));

        assertEquals(BigDecimal.ZERO, account.getAccruedInterest());
        assertEquals(BigDecimal.valueOf(100), account.getDebt());      // долг после снятия
        assertEquals(BigDecimal.valueOf(900), account.getBalance());   // баланс уменьшился
        assertEquals(BigDecimal.valueOf(100), account.getTotalDebt());
    }

    /**
     * Проверяет, что у нового аккаунта без долга и процентов
     * при запуске расчёта всё остаётся по нулям.
     */
    @Test
    void testAccrueInterestOnEmptyAccountDoesNothing() {
        account.setBalance(BigDecimal.valueOf(1200));

        Page<CreditAccount> page = new PageImpl<>(List.of(account));
        when(creditAccountRepository.findAll(any(Pageable.class))).thenReturn(page);

        creditAccountService.accrueMonthlyInterest();

        assertEquals(BigDecimal.ZERO, account.getAccruedInterest());
        assertEquals(BigDecimal.ZERO, account.getDebt());
        assertEquals(BigDecimal.valueOf(1200), account.getBalance());
        assertEquals(BigDecimal.ZERO, account.getTotalDebt());
    }





}
