package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private static final Random random = new Random();

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account save(CreateAccountDto accountDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (accountDto.getBlocked() == null) {
            accountDto.setBlocked(false);
        }

        Account account = AccountMapper.toEntity(accountDto, user, generateUniqueId());
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            account.setBalance(BigDecimal.ZERO);
        }
        Account savedAccount = accountRepository.save(account);

        if (user.getMainAccount() == null) {
            user.setMainAccount(savedAccount);
            userRepository.save(user);
        }

        return savedAccount;
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account update(Account account) {
        return accountRepository.save(account);
    }

    public void setUserByUserId(Long userId, Account account) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        account.setUser(user);
    }

    public void deposit(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    public void withdraw(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InvalidOperationException("Not enough funds");
        }

        if (Boolean.TRUE.equals(account.getBlocked())) {
            throw new InvalidOperationException("Account is blocked");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    public long generateUniqueId() {
        long number;
        do {
            number = 10000000 + random.nextInt(90000000);
        } while (accountRepository.existsById(number));
        return number;
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public TransferResponseDto transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", fromAccountId));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", toAccountId));

        if (Boolean.TRUE.equals(fromAccount.getBlocked())) {
            throw new InvalidOperationException("Sender account is blocked");
        }

        if (amount.compareTo(fromAccount.getBalance()) > 0) {
            throw new InvalidOperationException("Not enough funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new TransferResponseDto(
                fromAccount.getUser().getUserId(),
                fromAccount.getId(),
                fromAccount.getBalance(),
                toAccount.getUser().getUserId(),
                toAccount.getId(),
                toAccount.getBalance(),
                comment
        );
    }

    public TransferResponseDto transferByUserId(Long fromAccId, Long toUserId, BigDecimal amount, String comment) {
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", toUserId));

        return transfer(fromAccId, toUser.getMainAccount().getId(), amount, comment);
    }

    public TransferResponseDto transferByPhone(Long fromAccId, String toPhone, BigDecimal amount, String comment) {
        User toUser = userRepository.findByPhoneNumber(toPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", toPhone));

        return transfer(fromAccId, toUser.getMainAccount().getId(), amount, comment);
    }

    public List<Account> findByUserId(Long userId) {
        return accountRepository.findByUserUserId(userId);
    }
}
