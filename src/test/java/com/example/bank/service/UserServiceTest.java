package com.example.bank.service;

import com.example.bank.Enums.Role;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.account.debitAccount.DebitAccount;
import com.example.bank.Enums.AccountType;
import com.example.bank.model.user.CreateUserDto;
import com.example.bank.model.user.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;
    
    private DebitAccount account;



    @InjectMocks
    private UserService userService;

    private User expectedUser;
    private CreateUserDto createUserDto;

    @BeforeEach
    void setUp() {



        // Подготовка DTO
        createUserDto = new CreateUserDto();
        createUserDto.setFirstName("John");
        createUserDto.setLastName("Doe");
        createUserDto.setEmail("john.doe@example.com");
        createUserDto.setUsername("johndoe");
        createUserDto.setPassword("password");
        createUserDto.setBlocked(null); // Проверим, что сервис установит false
        createUserDto.setRole(Role.USER);

        // Подготовка ожидаемого пользователя
        expectedUser = new User();
        expectedUser.setUserId(1L);
        expectedUser.setFirstName("John");
        expectedUser.setLastName("Doe");
        expectedUser.setEmail("john.doe@example.com");
        expectedUser.setUsername("johndoe");
        expectedUser.setPassword("encodedPassword");
        expectedUser.setBlocked(false);
        expectedUser.setRole(Role.USER);


        account = new DebitAccount();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setUser(expectedUser);
    }

    @Test
    void testCreateUser() {
        // Arrange - настройка поведения моков
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act - вызов тестируемого метода
        User actualUser = userService.createUser(createUserDto);

        // Assert - проверка результатов
        assertNotNull(actualUser);
        assertEquals(expectedUser.getUserId(), actualUser.getUserId());
        assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(), actualUser.getLastName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        assertEquals("encodedPassword", actualUser.getPassword());
        assertEquals(false, actualUser.getBlocked());
        assertEquals(Role.USER, actualUser.getRole());

        // Verify - проверка вызовов методов моков
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUserWithBlockedSetToNull() {
        // Arrange
        createUserDto.setBlocked(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User actualUser = userService.createUser(createUserDto);

        // Assert - проверяем, что blocked установлен в false
        assertFalse(actualUser.getBlocked());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(expectedUser));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(1).contains(expectedUser);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        User user = userService.getUserById(1L);

        assertEquals(expectedUser, user);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void setMainAccountTest() {
        // Мокаем SecurityContext и Authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(expectedUser);

        SecurityContextHolder.setContext(securityContext);

        // Мокаем репозитории
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(expectedUser));
        when(accountRepository.findByAccountNumberAndAccountType("1234567890", AccountType.DEBIT))
                .thenReturn(Optional.of(account));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);


        userService.setMainAccount("1234567890");
        DebitAccount account2 = expectedUser.getMainAccount();
        assertEquals(account2, account);
    }

    @Test
    void deleteUserById_UserExists_DeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserById_UserNotExists_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUserById(99L);
        });

        verify(userRepository, never()).deleteById(anyLong());
    }





}
