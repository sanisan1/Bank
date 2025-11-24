package com.example.bank.integration;

import com.example.bank.model.account.creditAccount.CreditAccountCreateRequest;
import com.example.bank.model.transaction.TransactionOperationRequest;
import com.example.bank.model.transaction.TransferRequest;
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
public class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private String adminToken;
    private String userToken;
    private String creditAccountNumber;
    private String debitAccountNumber;

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

        String creditAccountResponse = mockMvc.perform(post("/api/credit-accounts/createforadmin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Извлекаем номер кредитного счета из ответа
        JsonNode creditAccountJson = objectMapper.readTree(creditAccountResponse);
        creditAccountNumber = creditAccountJson.get("accountNumber").asText();

        // Создаем дебетовый аккаунт
        String debitAccountResponse = mockMvc.perform(post("/api/debit-accounts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Извлекаем номер дебетового счета из ответа
        JsonNode debitAccountJson = objectMapper.readTree(debitAccountResponse);
        debitAccountNumber = debitAccountJson.get("accountNumber").asText();
    }

    @Test
    public void depositToAccount_asUser_succeeds() throws Exception {
        // Подготовка данных для депозита
        TransactionOperationRequest request = new TransactionOperationRequest();
        request.setAccountNumber(debitAccountNumber);
        request.setAmount(new BigDecimal("100.00"));
        request.setComment("Test deposit");

        // Выполняем депозит
        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(debitAccountNumber))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    public void withdrawFromAccount_asUser_succeeds() throws Exception {
        // Сначала пополняем счет
        TransactionOperationRequest depositRequest = new TransactionOperationRequest();
        depositRequest.setAccountNumber(debitAccountNumber);
        depositRequest.setAmount(new BigDecimal("100.00"));
        depositRequest.setComment("Initial deposit");

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Затем снимаем средства
        TransactionOperationRequest withdrawRequest = new TransactionOperationRequest();
        withdrawRequest.setAccountNumber(debitAccountNumber);
        withdrawRequest.setAmount(new BigDecimal("50.00"));
        withdrawRequest.setComment("Test withdrawal");

        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(debitAccountNumber))
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    public void transferBetweenAccounts_asUser_succeeds() throws Exception {
        // Сначала пополняем первый счет
        TransactionOperationRequest depositRequest = new TransactionOperationRequest();
        depositRequest.setAccountNumber(debitAccountNumber);
        depositRequest.setAmount(new BigDecimal("200.00"));
        depositRequest.setComment("Initial deposit");

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Переводим средства со счета на счет
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccount(debitAccountNumber);
        transferRequest.setToAccount(creditAccountNumber);
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setComment("Test transfer");

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(debitAccountNumber))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    public void getTransactionsByAccount_asUser_succeeds() throws Exception {
        // Сначала выполняем операцию, чтобы были транзакции
        TransactionOperationRequest depositRequest = new TransactionOperationRequest();
        depositRequest.setAccountNumber(debitAccountNumber);
        depositRequest.setAmount(new BigDecimal("100.00"));
        depositRequest.setComment("Test deposit");

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Получаем список транзакций
        mockMvc.perform(get("/api/transactions/by-account/{accountNumber}", debitAccountNumber)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].toAccount").value(debitAccountNumber))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    public void getAllTransactions_asAdmin_succeeds() throws Exception {
        // Сначала выполняем операцию, чтобы были транзакции
        TransactionOperationRequest depositRequest = new TransactionOperationRequest();
        depositRequest.setAccountNumber(debitAccountNumber);
        depositRequest.setAmount(new BigDecimal("100.00"));
        depositRequest.setComment("Test deposit");

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Админ получает все транзакции
        mockMvc.perform(get("/api/transactions/getAll")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void getAllTransactions_asUser_forbidden() throws Exception {
        // Обычный пользователь пытается получить все транзакции
        mockMvc.perform(get("/api/transactions/getAll")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}