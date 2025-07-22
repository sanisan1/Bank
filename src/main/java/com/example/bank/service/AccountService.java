package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private static final Random random = new Random();
    private final AccountSecurity accountSecurity;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountSecurity accountSecurity, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountSecurity = accountSecurity;
        this.transactionRepository = transactionRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return user;

    }
    @Transactional
    public Account save(CreateAccountDto accountDto) {

        User user = getCurrentUser();

        if (accountDto.getBlocked() == null) {
            accountDto.setBlocked(false);
        }

        Account account = AccountMapper.toEntity(accountDto, user, generateUniqueId());
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<Account> findAll() {
        User user = getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return accountRepository.findAll();
        } else {
            return accountRepository.findByUserUserId(user.getUserId());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Account update(Account account) {
        return accountRepository.save(account);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void setUserByUserId(Long userId, Account account) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        account.setUser(user);
    }
    @PreAuthorize("@accountSecurity.isOwner(#id)")
    public void deposit(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setOperationType(OperationType.deposit);
        transaction.setAmount(amount);
        transaction.setToAccount(account);
        transaction.setToUser(account.getUser());
        transactionRepository.save(transaction);

    }
    @PreAuthorize("@accountSecurity.isOwner(#id)")
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

    private long generateUniqueId() {
        long number;
        do {
            number = 10000000 + random.nextInt(90000000);
        } while (accountRepository.existsById(number));
        return number;
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public TransferResponseDto transfer(Long fromAccId, Long toAccountId, BigDecimal amount, String comment) {
        User currentUser = getCurrentUser();
        if (fromAccId == null) {
            if (currentUser.getMainAccount() == null) {
                throw new InvalidOperationException("Main account is not set for current user");
            }
            fromAccId = currentUser.getMainAccount().getId();
        } else {
            if (!accountSecurity.isOwner(fromAccId)) {
                throw new InvalidOperationException("Not owner acount");
            }
        }
        Long resolvedFromAccId = fromAccId;
        if (resolvedFromAccId == null) {

            if (currentUser.getMainAccount() == null) {
                throw new InvalidOperationException("Main account is not set for current user");
            }
            resolvedFromAccId = currentUser.getMainAccount().getId();
        }

        final Long finalFromAccId = resolvedFromAccId; //делаем финальной для использования в лямбде

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        Account fromAccount = accountRepository.findById(finalFromAccId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", finalFromAccId));

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

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setComment(comment);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setFromUser(fromAccount.getUser());
        transaction.setToUser(toAccount.getUser());
        transaction.setOperationType(OperationType.transfer);
        transactionRepository.save(transaction);



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
