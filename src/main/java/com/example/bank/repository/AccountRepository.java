package com.example.bank.repository;

import com.example.bank.model.Account.DebitAccount.Account;
import com.example.bank.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsById(Long accountNumber);
    // Здесь можно добавить свои методы поиска, например:
    List<Account> findByUserUserId(Long userId);

    boolean existsByAccountNumber(String number);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAndAccountType(String accountNumber, AccountType type);

    void deleteByAccountNumber(String accountNumber);

    List<Account> findByUserUserIdAndAccountType(Long userId, String accountType);
}
