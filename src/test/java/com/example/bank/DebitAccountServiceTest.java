package com.example.bank;

import com.example.bank.Enums.Role;
import com.example.bank.model.Account.Account;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.User.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.DebitAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class DebitAccountServiceTest {

    @Autowired
    private DebitAccountService debitAccountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        // создаём реального пользователя в базе
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("123");
        testUser.setRole(Role.USER);// обязательно!
        userRepository.save(testUser);


        // создаём дебетовый аккаунт для пользователя
        DebitAccount account = new DebitAccount();
        account.setUser(testUser);
        account.setAccountNumber(debitAccountService.generateUniqueAccountNumber());
        account.setBalance(BigDecimal.valueOf(10000));
        accountRepository.save(account);

        DebitAccount account2 = new DebitAccount();
        account2.setUser(testUser);
        account2.setAccountNumber(debitAccountService.generateUniqueAccountNumber());
        account2.setBalance(BigDecimal.valueOf(0));
        accountRepository.save(account2);
    }

    @Test
    @WithMockUser(username = "testuser") // подставляем нашего пользователя в SecurityContext
    void depositTest() {
        // получаем аккаунт пользователя
        DebitAccount account = (DebitAccount) accountRepository.findByUserUserId(testUser.getUserId())
                .get(0);

        // делаем депозит
        debitAccountService.deposit(account.getAccountNumber(), BigDecimal.valueOf(10000));

        // проверяем обновлённый баланс
        DebitAccount updated = (DebitAccount) accountRepository.findByAccountNumber(account.getAccountNumber())
                .orElseThrow();
        assertEquals(BigDecimal.valueOf(20000), updated.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser") // подставляем нашего пользователя в SecurityContext
    void withdrawTest() {
        // получаем аккаунт пользователя
        DebitAccount account = (DebitAccount) accountRepository.findByUserUserId(testUser.getUserId())
                .get(0);

        // делаем депозит
        debitAccountService.withdraw(account.getAccountNumber(), BigDecimal.valueOf(10000));

        // проверяем обновлённый баланс
        DebitAccount updated = (DebitAccount) accountRepository.findByAccountNumber(account.getAccountNumber())
                .orElseThrow();
        assertEquals(BigDecimal.valueOf(0), updated.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser")
    void transferTest() {
        DebitAccount account = (DebitAccount) accountRepository.findByUserUserId(testUser.getUserId())
                .get(0);
        DebitAccount account2 = (DebitAccount) accountRepository.findByUserUserId(testUser.getUserId())
                .get(1);
        String fromAcc = account.getAccountNumber();
        String toAcc = account2.getAccountNumber();
        debitAccountService.transfer(fromAcc, toAcc, BigDecimal.valueOf(1500), "1500 на другой счёт");

        DebitAccount updated = (DebitAccount) accountRepository.findByAccountNumber(fromAcc)
                .orElseThrow();

        DebitAccount updated2 = (DebitAccount) accountRepository.findByAccountNumber(toAcc)
                .orElseThrow();

        assertEquals(BigDecimal.valueOf(8500), updated.getBalance());
        assertEquals(BigDecimal.valueOf(1500), updated2.getBalance());

    }

    @Test
    @WithMockUser(username = "testuser")
    void createAccountTest() {
         debitAccountService.createAccount();
        DebitAccount account = (DebitAccount) accountRepository.findByUserUserId(testUser.getUserId())
                .get(3);


        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(10, account.getAccountNumber().length());

    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteAccountTest() {
        // создаем аккаунт
        debitAccountService.createAccount();

        // получаем список аккаунтов до удаления
        List<Account> beforeDelete = accountRepository.findByUserUserId(testUser.getUserId());
        int initialSize = beforeDelete.size();

        // берём конкретный аккаунт
        DebitAccount account = (DebitAccount) beforeDelete.get(initialSize - 2);

        // удаляем аккаунт
        debitAccountService.deleteAccount(account.getAccountNumber());

        // получаем список аккаунтов после удаления
        List<Account> afterDelete = accountRepository.findByUserUserId(testUser.getUserId());

        // проверяем, что размер уменьшился на 1
        assertThat(afterDelete).hasSize(initialSize - 1);

        // проверяем, что удаленного аккаунта нет
        assertThat(afterDelete)
                .noneMatch(acc -> acc.getAccountNumber().equals(account.getAccountNumber()));
    }

}

