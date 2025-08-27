package com.example.bank.repository;

import com.example.bank.model.Transaction.Transfers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transfers, Long> {
}
