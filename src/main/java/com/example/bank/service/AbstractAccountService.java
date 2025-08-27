package com.example.bank.service;

import com.example.bank.exception.AccountBlockedException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.exception.UserBlockedException;
import com.example.bank.model.Account.DebitAccount.Account;
import com.example.bank.model.Role;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public abstract class AbstractAccountService {

    protected final AccountRepository accountRepository;
    protected final UserRepository userRepository;
    protected final AccountSecurity accountSecurity;

    public AbstractAccountService(AccountRepository accountRepository,
                                  UserRepository userRepository,
                                  AccountSecurity accountSecurity) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountSecurity = accountSecurity;
    }

    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    protected void checkUserBlock(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (Boolean.TRUE.equals(user.getBlocked())) throw new UserBlockedException(user);
    }
    protected void checkAccountBlock(Account account) {
        if (account == null) throw new IllegalArgumentException("User cannot be null");
        if (Boolean.TRUE.equals(account.getBlocked())) throw new AccountBlockedException(account);
    }

    public String generateUniqueAccountNumber() {
        String number;
        do {
            long randomNum = (long) (Math.random() * 1_000_000_0000L);
            number = String.format("%010d", randomNum);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }

    public List<Account> findAll(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return accountRepository.findAll();
        } else {
            return accountRepository.findByUserUserId(currentUser.getUserId());
        }
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    public Account update(Account account) {
        return accountRepository.save(account);
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    //админские методы
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Account blockAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setBlocked(true);
        return accountRepository.save(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Account unblockAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setBlocked(false);
        return accountRepository.save(account);
    }

    protected Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
    }

}
