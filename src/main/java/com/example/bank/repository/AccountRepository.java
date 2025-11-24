package com.example.bank.repository;

import com.example.bank.model.Account.Account;
import com.example.bank.Enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsById(Long accountNumber);
    List<Account> findByUserUserId(Long userId);

    boolean existsByAccountNumber(String number);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAndAccountType(String accountNumber, AccountType type);

    void deleteByAccountNumber(String accountNumber);

    List<Account> findByUserUserIdAndAccountType(Long userId, String accountType);
}
