package com.example.bank.service;

import com.example.bank.exception.UserBlockedException;
import com.example.bank.mapper.DebitAccountMapper;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.Account.DebitAccount.DebitAccountResponse;
import com.example.bank.model.User.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebitAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountSecurity accountSecurity;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DebitAccountService debitAccountService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createAccount_shouldCreateDebitAccount() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setBlocked(false);

        DebitAccount account = new DebitAccount();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setUser(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(DebitAccount.class))).thenReturn(account);

        // Act
        DebitAccountResponse response = debitAccountService.createAccount();

        // Assert
        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        verify(accountRepository, times(1)).save(any(DebitAccount.class));
    }

    @Test
    void createAccount_shouldThrowExceptionWhenUserBlocked() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setBlocked(true);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(UserBlockedException.class, () -> debitAccountService.createAccount());
        verify(accountRepository, never()).save(any(DebitAccount.class));
    }

    @Test
    void createAccount_shouldThrowExceptionWhenUserNotAuthenticated() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> debitAccountService.createAccount());
        verify(accountRepository, never()).save(any(DebitAccount.class));
    }
}