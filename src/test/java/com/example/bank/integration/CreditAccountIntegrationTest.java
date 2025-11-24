package com.example.bank.integration;

import com.example.bank.model.account.creditAccount.CreditAccountCreateRequest;
import com.example.bank.model.user.CreateUserDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "bank.credit.default.interest-rate=15.0",
        "bank.credit.default.limit=10000.0",
        "bank.credit.default.minimum-payment-rate=5.0",
        "bank.credit.default.grace-period=30"
})
public class CreditAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private String adminToken;
    private String userToken;
    private String creditAccountNumber;

    @BeforeEach
    public void setUp() throws Exception {
        // Получаем токен администратора
        adminToken = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.example.bank.model.user.LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Создаем обычного пользователя
        CreateUserDto userDto = new CreateUserDto();
        userDto.setUsername("testuser2");
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setEmail("testuser@example.com");
        userDto.setPhoneNumber("1234567890");

        String userResponse = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Получаем токен обычного пользователя
        userToken = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.example.bank.model.user.LoginRequest("testuser2", "password123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Извлекаем ID пользователя из ответа
        JsonNode userJson = objectMapper.readTree(userResponse);
        userId = userJson.get("userId").asLong();

        // Создаем кредитный аккаунт через администратора
        CreditAccountCreateRequest request = new CreditAccountCreateRequest();
        request.setUserId(userId);
        request.setCreditLimit(new BigDecimal("5000.00"));
        request.setInterestRate(new BigDecimal("0.15"));
        request.setGracePeriod(30);

        String accountResponse = mockMvc.perform(post("/api/credit-accounts/createforadmin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Извлекаем номер счета из ответа
        JsonNode accountJson = objectMapper.readTree(accountResponse);
        creditAccountNumber = accountJson.get("accountNumber").asText();
    }

    @Test
    public void createCreditAccount_asUser_createsAccountWithDefaultValues() throws Exception {
        // Создаем нового пользователя без кредитного аккаунта
        CreateUserDto newUserDto = new CreateUserDto();
        newUserDto.setUsername("newuser");
        newUserDto.setPassword("password123");
        newUserDto.setFirstName("New");
        newUserDto.setLastName("User");
        newUserDto.setEmail("newuser@example.com");
        newUserDto.setPhoneNumber("9876543210");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isOk());

        // Получаем токен нового пользователя
        String newUserToken = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.example.bank.model.user.LoginRequest("newuser", "password123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Новый пользователь создает себе кредитный аккаунт
        mockMvc.perform(post("/api/credit-accounts/create")
                        .header("Authorization", "Bearer " + newUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.creditLimit").value(10000.00))
                .andExpect(jsonPath("$.interestRate").value(15.0))
                .andExpect(jsonPath("$.gracePeriod").value(30));
    }




    //Пользователь не может создать второй аккаунт
    @Test
    public void createCreditAccount_asUser_returnsForbidden() throws Exception {

        userToken = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.example.bank.model.user.LoginRequest("testuser2", "password123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();



        // Попытка создания кредитного аккаунта обычным пользователем
        mockMvc.perform(post("/api/credit-accounts/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void increaseCreditLimit_asAdmin_increasesLimit() throws Exception {
        // Увеличиваем кредитный лимит
        mockMvc.perform(put("/api/credit-accounts/{accountNumber}/increase-limit", creditAccountNumber)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("newLimit", "6000.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditLimit").value(6000.00));
    }

    @Test
    public void increaseCreditLimit_asUser_returnsForbidden() throws Exception {
        // Пытаемся увеличить кредитный лимит как обычный пользователь
        mockMvc.perform(put("/api/credit-accounts/{accountNumber}/increase-limit", creditAccountNumber)
                        .header("Authorization", "Bearer " + userToken)
                        .param("newLimit", "6000.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void accrueInterest_succeeds() throws Exception {
        mockMvc.perform(post("/api/credit-accounts/accrue-interest")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
