package com.example.bank.repository;

import com.example.bank.model.account.creditAccount.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {

    Optional<CreditAccount>findByAccountNumber(String accountNumber);

    List<CreditAccount> findByUserUserId(Long userId);
}
