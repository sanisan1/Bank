package com.example.bank.model.Transaction;

import com.example.bank.Enums.OperationType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Data
@NoArgsConstructor
public class TransactionDto {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private OperationType type;
    private String comment;



}