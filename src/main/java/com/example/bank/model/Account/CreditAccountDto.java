package com.example.bank.model.Account;

import com.example.bank.model.Account.DebitAccount.AccountDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditAccountDto extends AccountDto {

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal balance;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be positive")
    private BigDecimal creditLimit;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate cannot be negative")
    private BigDecimal interestRate;

    @NotNull
    @DecimalMin(value = "1.0", inclusive = true, message = "Minimum payment must be at least 1%")
    @DecimalMin(value = "0.0", inclusive = true)
    @Max(value = 100, message = "Minimum payment cannot exceed 100%")
    private BigDecimal minimumPaymentRate;

    @NotNull
    @Min(value = 0, message = "Grace period cannot be negative")
    private Integer gracePeriod;

    @NotNull
    private LocalDate paymentDueDate;

    // геттеры и сеттеры
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getMinimumPaymentRate() { return minimumPaymentRate; }
    public void setMinimumPaymentRate(BigDecimal minimumPaymentRate) { this.minimumPaymentRate = minimumPaymentRate; }

    public Integer getGracePeriod() { return gracePeriod; }
    public void setGracePeriod(Integer gracePeriod) { this.gracePeriod = gracePeriod; }

    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }
}
