package com.example.bank.controller;


import com.example.bank.model.account.Account;

import com.example.bank.service.AccountServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountServiceImpl accountService;

    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{accountNumber}/block")
    public ResponseEntity blockAccount(@PathVariable String accountNumber) {
        Account account = accountService.blockAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{accountNumber}/unblock")
    public ResponseEntity unblockAccount(@PathVariable String accountNumber) {
        Account account = accountService.unblockAccount(accountNumber);
        return ResponseEntity.ok(account);
    }


}
