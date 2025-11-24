package com.example.bank.controller;

import com.example.bank.model.transaction.TransactionOperationRequest;
import com.example.bank.model.transaction.TransactionResponse;

import com.example.bank.model.account.AccountDto;
import com.example.bank.model.transaction.TransferRequest;
import com.example.bank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Операции
    @PostMapping("/deposit")
    public AccountDto deposit(@RequestBody TransactionOperationRequest request) {
        return transactionService.deposit(
                request.getAccountNumber(),
                request.getAmount(),
                request.getComment()
        );
    }

    @PostMapping("/withdraw")
    public AccountDto withdraw(@RequestBody TransactionOperationRequest request) {
        return transactionService.withdraw(
                request.getAccountNumber(),
                request.getAmount(),
                request.getComment()
        );
    }

    @PostMapping("/transfer")
    public AccountDto transfer(@RequestBody TransferRequest request) {
        return transactionService.transfer(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getComment()
        );
    }

    // Запросы данных
    @GetMapping("/by-account/{accountNumber}")
    public List<TransactionResponse> getTransactionsByAccount(@PathVariable String accountNumber) {
        return transactionService.getTransactionsByAccount(accountNumber);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/by-user/{userId}")
    public List<TransactionResponse> getTransactionsByUser(@PathVariable Long userId) {
        return transactionService.getTransactionsByUser(userId);
    }
}