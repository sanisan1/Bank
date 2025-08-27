package com.example.bank.repository;

import com.example.bank.model.Account.DebitAccount.DebitAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<DebitAccount, Long> {
    boolean existsById(Long accountNumber);
    // Здесь можно добавить свои методы поиска, например:
    List<DebitAccount> findByUserUserId(Long userId);

    boolean existsByAccountNumber(String number);

    Optional<DebitAccount> findByAccountNumber(String accountNumber);
}
