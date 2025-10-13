package com.example.bank.controller;

import com.example.bank.model.Account.AccountOperationRequest;
import com.example.bank.model.Account.DebitAccount.*;
import com.example.bank.model.Transaction.TransferRequest;
import com.example.bank.model.Transaction.TransferResponseDto;
import com.example.bank.service.DebitAccountService;
import com.example.bank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debit-accounts")
public class DebitAccountController {

    private final DebitAccountService debitAccountService;
    private final UserService userService;

    public DebitAccountController(DebitAccountService debitAccountService, UserService userService) {
        this.debitAccountService = debitAccountService;
        this.userService = userService;
    }

    // ✅ Создание аккаунта
    @PostMapping
    public ResponseEntity<DebitAccountResponse> createAccount() {
        DebitAccountResponse created = debitAccountService.createAccount();
        return ResponseEntity.status(201).body(created);
    }

    // ✅ Пополнение
    @PostMapping("/deposit")
    public ResponseEntity<DebitAccountResponse> deposit(@Valid @RequestBody AccountOperationRequest request) {
        DebitAccountResponse response = debitAccountService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    // ✅ Снятие
    @PostMapping("/withdraw")
    public ResponseEntity<DebitAccountResponse> withdraw(@Valid @RequestBody AccountOperationRequest request) {
        DebitAccountResponse response = debitAccountService.withdraw(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    // ✅ Перевод
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponseDto response = debitAccountService.transfer(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getComment()
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Удаление аккаунта
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        debitAccountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
