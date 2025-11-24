package com.example.bank.service;

import com.example.bank.exception.AccountBlockedException;
import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.Account.Account;
import com.example.bank.Enums.Role;
import com.example.bank.model.User.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

// Абстрактный сервис для управления банковскими счетами
@Transactional
public abstract class AbstractAccountService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    protected final AccountRepository accountRepository;
    protected final UserRepository userRepository;
    protected final AccountSecurity accountSecurity;
    private final SecureRandom secureRandom = new SecureRandom();

    public AbstractAccountService(AccountRepository accountRepository,
                                  UserRepository userRepository,
                                  AccountSecurity accountSecurity) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountSecurity = accountSecurity;
    }

    // Получает текущего аутентифицированного пользователя

    protected User getCurrentUser() {
        log.info("Retrieving current user from security context");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                log.error("Unauthenticated access attempt");
                throw new AccessDeniedException("User is not authenticated");
            }
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found with username={}", username);
                        return new ResourceNotFoundException("User", "username", username);
                    });
        } catch (Exception e) {
            log.error("Error retrieving user: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Проверяет, заблокирован ли счет
    protected void checkAccountBlock(Account account) {
        if (account == null) {
            log.error("Account argument is null!");
            throw new IllegalArgumentException("Счет не может быть null");
        }
        if (Boolean.TRUE.equals(account.getBlocked())) {
            log.error("Operation attempt on blocked account {}", account.getAccountNumber());
            throw new AccountBlockedException(account);
        }
    }

    // Генерирует уникальный 10-значный номер счета
    public String generateUniqueAccountNumber() {
        log.info("Generating unique account number");
        String number;
        long bound = 1_000_000_0000L;
        try {
            do {
                long randomNum = Math.abs(secureRandom.nextLong()) % bound;
                number = String.format("%010d", randomNum);
            } while (accountRepository.existsByAccountNumber(number));
            return number;
        } catch (Exception e) {
            log.error("Error generating account number: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Находит счет по его номеру
    public Account getAccountByNumber(String accountNumber) {
        log.info("Searching account by number: {}", accountNumber);
        try {
            return accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> {
                        log.error("Account not found with number {}", accountNumber);
                        return new ResourceNotFoundException("Account", "accountNumber", accountNumber);
                    });
        } catch (Exception e) {
            log.error("Error searching account by number: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Возвращает все счета пользователя или все счета для администратора
    public List<Account> findAll(User currentUser) {
        log.info("Retrieving accounts list for user: {}", currentUser.getUsername());
        try {
            if (currentUser.getRole() == Role.ADMIN) {
                return accountRepository.findAll();
            } else {
                return accountRepository.findByUserUserId(currentUser.getUserId());
            }
        } catch (Exception e) {
            log.error("Error retrieving accounts list: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Находит счет по ID
    public Account findById(Long id) {
        log.info("Searching account by ID: {}", id);
        try {
            return accountRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Account not found with ID {}", id);
                        return new ResourceNotFoundException("Account", "id", id);
                    });
        } catch (Exception e) {
            log.error("Error searching account by ID: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Обновляет информацию о счете
    public Account update(Account account) {
        log.info("Updating account with ID: {}", account.getAccountNumber());
        try {
            return accountRepository.save(account);
        } catch (Exception e) {
            log.error("Error updating account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Удаляет счет по ID
    public void deleteById(Long id) {
        log.info("Deleting account by ID: {}", id);
        try {
            accountRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting account by ID: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Блокирует счет (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Account blockAccount(String accountNumber) {
        log.info("Admin blocking account: {}", accountNumber);
        try {
            Account account = getAccountByNumber(accountNumber);
            account.setBlocked(true);
            return accountRepository.save(account);
        } catch (Exception e) {
            log.error("Error while blocking account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Разблокирует счет (только для администратора)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Account unblockAccount(String accountNumber) {
        log.info("Admin unblocking account: {}", accountNumber);
        try {
            Account account = getAccountByNumber(accountNumber);
            account.setBlocked(false);
            return accountRepository.save(account);
        } catch (Exception e) {
            log.error("Error while unblocking account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Получает счет по ID
    protected Account getAccountById(Long accountId) {
        try {
            return accountRepository.findById(accountId)
                    .orElseThrow(() -> {
                        log.error("Account not found with ID {}", accountId);
                        return new ResourceNotFoundException("Account", "id", accountId);
                    });
        } catch (Exception e) {
            log.error("Error getting account by ID: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Обрабатывает операцию пополнения счета
    protected Account processDeposit(Account account, BigDecimal amount) {
        log.info("Depositing to account {} amount {}", account.getAccountNumber(), amount);
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Deposit attempt with non-positive amount: {}", amount);
                throw new IllegalArgumentException("Сумма пополнения должна быть больше нуля");
            }
            checkAccountBlock(account);

            account.setBalance(account.getBalance().add(amount));
            return account;
        } catch (Exception e) {
            log.error("Error depositing to account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Обрабатывает операцию снятия со счета
    protected Account processWithdraw(Account account, BigDecimal amount) {
        log.info("Withdrawing from account {} amount {}", account.getAccountNumber(), amount);
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Withdrawal attempt with non-positive amount: {}", amount);
                throw new IllegalArgumentException("Сумма снятия должна быть больше нуля");
            }
            checkAccountBlock(account);
            BigDecimal balance = account.getBalance();
            if (amount.compareTo(balance) > 0) {
                log.error("Insufficient funds: account {}, balance {}, attempt to withdraw {}", account.getAccountNumber(), balance, amount);
                throw new InvalidOperationException("Недостаточно средств");
            }
            account.setBalance(balance.subtract(amount));
            return account;
        } catch (Exception e) {
            log.error("Error withdrawing from account: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Удаляет счет пользователем только если баланс равен нулю
    public void deleteAccount(String accountNumber) {
        log.info("User deleting account: {}", accountNumber);
        try {
            User user = getCurrentUser();
            Account account = getAccountByNumber(accountNumber);
            checkAccountBlock(account);

            if (!account.getUser().getUserId().equals(user.getUserId())) {
                log.error("Attempt to delete account by non-owner, userId={}, owner={}", user.getUserId(), account.getUser().getUserId());
                throw new AccessDeniedException("Пользователь не является владельцем счета");
            }

            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                log.error("Attempt to delete account with non-zero balance: {}, balance={}", account.getAccountNumber(), account.getBalance());
                throw new InvalidOperationException("Невозможно удалить счет с ненулевым балансом");
            }

            accountRepository.deleteByAccountNumber(accountNumber);
        } catch (Exception e) {
            log.error("Error deleting account by user: {}", e.getMessage(), e);
            throw e;
        }
    }
}