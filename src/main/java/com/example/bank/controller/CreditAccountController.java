package com.example.bank.controller;

import com.example.bank.model.Account.*;
import com.example.bank.model.Account.CreditAccount.CreditAccount;
import com.example.bank.model.Account.CreditAccount.CreditAccountCreateRequest;
import com.example.bank.model.Account.CreditAccount.CreditAccountResponseDto;
import com.example.bank.model.Transaction.TransferRequest;
import com.example.bank.model.Transaction.TransferResponseDto;
import com.example.bank.model.Transaction.Transfers;
import com.example.bank.service.CreditAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/credit-accounts")
public class CreditAccountController {

    private final CreditAccountService creditAccountService;

    public CreditAccountController(CreditAccountService creditAccountService) {
        this.creditAccountService = creditAccountService;
    }

    /* ----------------------- Создание аккаунта (только для админа) ----------------------- */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CreditAccountResponseDto> createAccount(
            @Valid @RequestBody CreditAccountCreateRequest request
    ) {
        CreditAccountResponseDto dto = creditAccountService.createAccount(
                request.getUserID(),
                request.getCreditLimit(),
                request.getInterestRate(),
                request.getGracePeriod()

        );
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /* ----------------------- Операции с аккаунтом ----------------------- */

    @PostMapping("/deposit")
    public ResponseEntity<CreditAccountResponseDto> deposit(@Valid @RequestBody AccountOperationRequest request) {
        CreditAccountResponseDto response = creditAccountService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CreditAccountResponseDto> withdraw(@Valid @RequestBody AccountOperationRequest request) {
        CreditAccountResponseDto response = creditAccountService.withdraw(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponseDto response = creditAccountService.transfer(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getComment()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        creditAccountService.deleteByAccountNumber(accountNumber);
        return ResponseEntity.noContent().build();
    }

    /* ----------------------- Админские методы ----------------------- */



    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{accountNumber}/increase-limit")
    public ResponseEntity<CreditAccount> increaseCreditLimit(
            @PathVariable String accountNumber,
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal newLimit
    ) {
        CreditAccount account = creditAccountService.increaseCreditLimit(accountNumber, newLimit);
        return ResponseEntity.ok(account);
    }


    @PutMapping("/{accountNumber}/decrease-limit")
    public ResponseEntity<CreditAccount> decreaseCreditLimit(
            @PathVariable String accountNumber,
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal newLimit
    ) {
        CreditAccount account = creditAccountService.decreaseCreditLimit(accountNumber, newLimit);
        return ResponseEntity.ok(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{accountId}/set-interest")
    public ResponseEntity<CreditAccount> setInterestRate(
            @PathVariable Long accountId,
            @RequestParam @NotNull @DecimalMin("0.0") BigDecimal newRate
    ) {
        CreditAccount account = creditAccountService.setInterestRate(accountId, newRate);
        return ResponseEntity.ok(account);
    }
}
