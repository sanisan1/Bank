    package com.example.bank.controller;

    import com.example.bank.mapper.AccountMapper;
    import com.example.bank.model.*;
    import com.example.bank.model.Account.*;
    import com.example.bank.model.Account.DebitAccount.AccountDto;
    import com.example.bank.model.Account.DebitAccount.AccountOperationRequest;
    import com.example.bank.model.Account.DebitAccount.DebitAccount;
    import com.example.bank.service.AccountService;
    import jakarta.validation.Valid;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.math.BigDecimal;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;


    @RestController
    @RequestMapping("/api/account")
    public class AccountController {

        private final AccountService accountService;


        public AccountController(AccountService accountService) {
            this.accountService = accountService;

        }

        // ✅ Создание аккаунта
        @PostMapping
        public ResponseEntity<AccountDto> createAccount() {

            DebitAccount created = accountService.createAccount();
            return ResponseEntity.status(201).body(AccountMapper.toDto(created));
        }

        @PostMapping("/transferByUserID")
        public ResponseEntity<TransferResponseDto> transferByUserId(@Valid @RequestBody TransferUserIdDto request) {
            TransferResponseDto result = accountService.transferByUserId(
                    request.getFromAccId(),
                    request.getToUserId(),
                    request.getAmount(),
                    request.getComment()
            );
            return ResponseEntity.ok(result);
        }

        @PostMapping("/transferByAccountId")
        public ResponseEntity<TransferResponseDto> transferByAccountId(@Valid @RequestBody TransferAccountIdDto request) {
            TransferResponseDto result = accountService.transfer(
                    request.getFromAccId(),
                    request.getToAccId(),
                    request.getAmount(),
                    request.getComment()
            );
            return ResponseEntity.ok(result);
        }
        @PostMapping("/transferByPhone")
        public ResponseEntity<TransferResponseDto> transferByPhone(@Valid @RequestBody TransferPhoneDto request) {
            TransferResponseDto result = accountService.transferByPhone(
                    request.getFromAccId(),
                    request.getPhone(),
                    request.getAmount(),
                    request.getComment()
            );
            return ResponseEntity.ok(result);
        }


        // ✅ Получить все аккаунты
        @GetMapping
        public ResponseEntity<List<AccountDto>> getAllAccounts() {
            List<AccountDto> dtos = accountService.findAll().stream()
                    .map(AccountMapper::toDto)
                    .toList();
            return ResponseEntity.ok(dtos);
        }

        // ✅ Получить аккаунт по id
        @GetMapping("/{id}")
        public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
            return accountService.findById(id)
                    .map(AccountMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<List<AccountDto>> getAccountsByUserId(@PathVariable Long userId) {
            List<AccountDto> accounts = accountService.findByUserId(userId).stream()
                    .map(AccountMapper::toDto)
                    .toList();
            return ResponseEntity.ok(accounts);
        }




        @PutMapping("/{id}")
        public ResponseEntity<AccountDto> updateAccountfull(@RequestBody DebitAccount account, @PathVariable Long id) {
            account.setId(id); // Убедись, что ID совпадает
            DebitAccount updated = accountService.update(account);
            return ResponseEntity.ok(AccountMapper.toDto(updated));
        }

        @PostMapping("/{accountNumber}/withdraw")
        public ResponseEntity<AccountDto> withdraw(@PathVariable String accountNumber,
                                                   @Valid @RequestBody AccountOperationRequest request) {
            AccountDto updated = accountService.withdraw(accountNumber,request.getAmount());
            return ResponseEntity.ok(updated);
        }

        @PostMapping("/{accountNumber}/deposit")
        public ResponseEntity<AccountDto> deposit(@PathVariable String accountNumber,
                                                  @Valid @RequestBody AccountOperationRequest request) {
            System.out.println("accountNumber = " + accountNumber);
            AccountDto updated = accountService.deposit(accountNumber, request.getAmount());


            return ResponseEntity.ok(updated);
        }

        @PatchMapping("/{id}")
        public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
            Optional<DebitAccount> optionalAccount = accountService.findById(id);
            if (optionalAccount.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            DebitAccount account = optionalAccount.get();

            updates.forEach((key, value) -> {
                switch (key) {
                    case "balance" -> {
                        if (value instanceof Number number) {
                            account.setBalance(BigDecimal.valueOf(number.doubleValue()));
                        }
                    }
                    case "blocked" -> {
                        if (value instanceof Boolean b) {
                            account.setBlocked(b);
                        }
                    }
                    case "userId" -> {
                        if (value instanceof Number userIdNum) {
                            accountService.setUserByUserId(userIdNum.longValue(), account);
                        }
                    }
                    // Можно добавить ещё поля при необходимости
                }
            });

            DebitAccount updated = accountService.update(account);
            return ResponseEntity.ok(AccountMapper.toDto(updated));
        }

    }
