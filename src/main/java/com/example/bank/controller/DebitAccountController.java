package com.example.bank.controller;

import com.example.bank.model.account.debitAccount.*;
import com.example.bank.service.DebitAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debit-accounts")
public class DebitAccountController {

    private final DebitAccountService debitAccountService;

    public DebitAccountController(DebitAccountService debitAccountService) {
        this.debitAccountService = debitAccountService;
    }

    // ✅ Создание аккаунта
    @PostMapping
    public ResponseEntity<DebitAccountResponse> createAccount() {
        DebitAccountResponse created = debitAccountService.createAccount();
        return ResponseEntity.status(201).body(created);
    }


    // ✅ Удаление аккаунта
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        debitAccountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
