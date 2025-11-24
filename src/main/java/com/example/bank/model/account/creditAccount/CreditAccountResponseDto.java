package com.example.bank.model.account.creditAccount;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditAccountResponseDto {

    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private BigDecimal minimumPaymentRate;
    private Integer gracePeriod;
    private BigDecimal totalDebt; // показываем только totalDebt
    private LocalDate paymentDueDate;


}
