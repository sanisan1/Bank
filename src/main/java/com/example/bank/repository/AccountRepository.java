package com.example.bank.repository;

import com.example.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsById(Long accountNumber);
    // Здесь можно добавить свои методы поиска, например:
    List<Account> findByUserUserId(Long userId);

}
