package com.example.bank.model.Transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class TransferRequest {
    @NotBlank
    String fromAccount;
    @NotBlank
    String toAccount;
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount;
    String comment;

}
