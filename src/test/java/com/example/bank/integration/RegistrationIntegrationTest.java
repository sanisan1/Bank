package com.example.bank.integration;

import com.example.bank.Enums.Role;
import com.example.bank.model.User.CreateUserDto;
import com.example.bank.model.User.User;
import com.example.bank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CreateUserDto dto;

    @BeforeEach
    void setUp() {

        dto = new CreateUserDto();
        dto.setUsername("testuser_valid");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("testuser_valid@example.com");

    }

    @Test
    public void registerUser_withValidData_createsUserInDatabaseAndReturnsUser() throws Exception {

        // Выполнение запроса
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username", is("testuser_valid")))
                .andExpect(jsonPath("$.email", is("testuser_valid@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.blocked", is(false)));

        // Проверка, что пользователь создан в базе данных
        User savedUser = userRepository.findByUsername("testuser_valid").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuser_valid", savedUser.getUsername());
        assertEquals("testuser_valid@example.com", savedUser.getEmail());
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals("1234567890", savedUser.getPhoneNumber());
        assertEquals(Role.USER, savedUser.getRole());
        assertFalse(savedUser.getBlocked());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    public void registerUser_withMissingRequiredFields_returnsBadRequest() throws Exception {
        // Подготовка данных с отсутствующими обязательными полями
        dto.setUsername(""); // Пустое имя пользователя
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("invalid-email"); // Неверный формат email

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        
        // Проверка, что пользователь не создан в базе данных
        assertFalse(userRepository.findByUsername("").isPresent());
    }

    @Test
    public void registerUser_withExistingUsername_returnsConflict() throws Exception {
        // Создание первого пользователя
        CreateUserDto dto1 = new CreateUserDto();
        dto1.setUsername("existinguser_conflict");
        dto1.setPassword("password123");
        dto1.setFirstName("Test");
        dto1.setLastName("User");
        dto1.setPhoneNumber("1234567890");
        dto1.setEmail("existinguser_conflict@example.com");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        // Попытка создания пользователя с тем же именем
        CreateUserDto dto2 = new CreateUserDto();
        dto2.setUsername("existinguser_conflict");
        dto2.setPassword("password456");
        dto2.setFirstName("Another");
        dto2.setLastName("User");
        dto2.setPhoneNumber("0987654321");
        dto2.setEmail("another@example.com");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict()); // Ожидаем ошибку конфликта из-за уникальности имени пользователя
    }

    @Test
    public void registerUser_passwordIsEncodedInDatabase() throws Exception {
        // Подготовка данных
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("secureuser_password");
        dto.setPassword("mypassword123");
        dto.setFirstName("Secure");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("secure_password@example.com");

        // Выполнение запроса
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Проверка, что пароль зашифрован в базе данных
        User savedUser = userRepository.findByUsername("secureuser_password").orElse(null);
        assertNotNull(savedUser);
        assertNotEquals("mypassword123", savedUser.getPassword()); // Пароль должен быть зашифрован
        assertTrue(savedUser.getPassword().startsWith("$2a$")); // BCrypt хэш начинается с $2a$
    }
    
    @Test
    public void registerUser_withInvalidEmailFormat_returnsBadRequest() throws Exception {
        // Подготовка данных с неверным форматом email
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("testuser_email");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("invalid-email-format"); // Неверный формат email

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        
        // Проверка, что пользователь не создан в базе данных
        assertFalse(userRepository.findByUsername("testuser_email").isPresent());
    }
}