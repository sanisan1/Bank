package com.example.bank.service;

import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.AccountMapper;
import com.example.bank.mapper.UserMapper;
import com.example.bank.model.Account.DebitAccount.AccountDto;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.AccountType;
import com.example.bank.model.CreateUserDto;
import com.example.bank.model.OperationType;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    public User createUser(CreateUserDto createUserDto) {
        if (createUserDto.getBlocked() == null) {
            createUserDto.setBlocked(false);
        }

        User user = UserMapper.toEntity(createUserDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    public User update(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new ResourceNotFoundException("User", "id", user.getUserId());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PreAuthorize("@accountSecurity.isOwner(#account.accountNumber)")
    public AccountDto setMainAccount(String accountNumber) {
        User user = getCurrentUser();
        user.setMainAccount((DebitAccount) accountRepository.findByAccountNumberAndAccountType(accountNumber, AccountType.DEBIT).
                orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountNumber)));
        userRepository.save(user);
        return AccountMapper.toDto(user.getMainAccount());

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
}
