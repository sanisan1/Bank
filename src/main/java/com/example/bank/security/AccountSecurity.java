package com.example.bank.security;

import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("accountSecurity")
public class AccountSecurity {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountSecurity(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long accountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        DebitAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        return account.getUser().getUsername().equals(username);

    }

    public boolean isOwner(String accountNumber) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        DebitAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountNumber));

        return account.getUser().getUsername().equals(username);

    }

}
