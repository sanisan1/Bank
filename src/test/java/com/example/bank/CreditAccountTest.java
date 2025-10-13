package com.example.bank;


import com.example.bank.Enums.Role;
import com.example.bank.model.Account.CreditAccount.CreditAccount;
import com.example.bank.model.User.User;
import com.example.bank.repository.CreditAccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.CreditAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class CreditAccountTest {

    @Autowired
    private CreditAccountRepository creditAccountRepository;
    @Autowired
    private CreditAccountService creditAccountService;
    @Autowired
    private UserRepository userRepository;


    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testadminuser");
        testUser.setPassword("123");
        testUser.setRole(Role.ADMIN);
        userRepository.save(testUser);


    }


    @Test
    @WithMockUser(username = "testadminuser", roles = {"ADMIN"})
    void WithDrawDepositTest() {
        creditAccountService.createAccount(testUser.getUserId(), BigDecimal.valueOf(10000), BigDecimal.valueOf(3), 1);
        CreditAccount account = creditAccountRepository.findByUserUserId(testUser.getUserId()).get(0);


        creditAccountService.withdraw(account.getAccountNumber(), BigDecimal.valueOf(7000));

        // важно обновить account из базы, иначе может остаться старый объект в памяти
        account = creditAccountRepository.findByAccountNumber(account.getAccountNumber()).orElseThrow();

        assertEquals(BigDecimal.valueOf(7000), account.getTotalDebt());
        assertEquals(BigDecimal.valueOf(3000), account.getBalance());
        assertEquals(BigDecimal.valueOf(3), account.getInterestRate());

        creditAccountService.deposit(account.getAccountNumber(), BigDecimal.valueOf(3000));
        account = creditAccountRepository.findByAccountNumber(account.getAccountNumber()).orElseThrow();
        assertEquals(BigDecimal.valueOf(4000), account.getTotalDebt());
    }

    @Test
    @WithMockUser(username = "testadminuser", roles = {"ADMIN"})
    void transferBetweenTwoAccounts_ShouldUpdateBalancesCorrectly() {
        // given: два кредитных аккаунта для одного юзера
        var accDto1 = creditAccountService.createAccount(testUser.getUserId(), BigDecimal.valueOf(10000), BigDecimal.valueOf(3), 1);
        var accDto2 = creditAccountService.createAccount(testUser.getUserId(), BigDecimal.valueOf(10000), BigDecimal.valueOf(3), 1);

        CreditAccount fromAcc = creditAccountRepository.findByAccountNumber(accDto1.getAccountNumber()).orElseThrow();
        CreditAccount toAcc   = creditAccountRepository.findByAccountNumber(accDto2.getAccountNumber()).orElseThrow();

        BigDecimal initialFromBalance = fromAcc.getBalance(); // 10000
        BigDecimal initialToBalance   = toAcc.getBalance();   // 10000

        // when: переводим 4000
        creditAccountService.transfer(fromAcc.getAccountNumber(), toAcc.getAccountNumber(), BigDecimal.valueOf(4000), "Test transfer");

        // then: пересчитываем из базы
        fromAcc = creditAccountRepository.findByAccountNumber(fromAcc.getAccountNumber()).orElseThrow();
        toAcc   = creditAccountRepository.findByAccountNumber(toAcc.getAccountNumber()).orElseThrow();

        assertEquals(initialFromBalance.subtract(BigDecimal.valueOf(4000)), fromAcc.getBalance());
        assertEquals(initialToBalance.add(BigDecimal.valueOf(4000)), toAcc.getBalance());
    }

    



}



