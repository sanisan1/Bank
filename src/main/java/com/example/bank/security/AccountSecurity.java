package com.example.bank.security;

import com.example.bank.Enums.Role;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.Account.Account;
import com.example.bank.model.User.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("accountSecurity")
public class AccountSecurity {
    private static final Logger logger = LoggerFactory.getLogger(AccountSecurity.class);
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountSecurity(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long accountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("No authentication found for account ID: {}", accountId);
            return false;
        }
        
        String username = auth.getName();
        logger.debug("Checking ownership for user: {} and account ID: {}", username, accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        boolean isOwner = account.getUser().getUsername().equals(username);
        logger.debug("Ownership check result: {}", isOwner);
        return isOwner;
    }

    public boolean isOwner(String accountNumber) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("No authentication found for account number: {}", accountNumber);
            return false;
        }
        
        String username = auth.getName();
        logger.debug("Checking ownership for user: {} and account number: {}", username, accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        boolean isOwner = account.getUser().getUsername().equals(username);
        logger.debug("Ownership check result: {}", isOwner);
        return isOwner;
    }

    public boolean isSelfOrAdmin(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        return currentUser.getRole() == Role.ADMIN || currentUser.getUserId().equals(userId);
    }
}