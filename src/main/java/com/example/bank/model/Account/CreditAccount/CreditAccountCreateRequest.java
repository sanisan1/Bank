package com.example.bank.model.Account.CreditAccount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreditAccountCreateRequest {
    @NotNull
    private Long userID;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.01", message = "Credit limit must be greater than zero")
    private BigDecimal creditLimit;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate must be positive")
    private BigDecimal interestRate;

    @NotNull(message = "Grace period is required")
    private Integer gracePeriod;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(Integer gracePeriod) {
        this.gracePeriod = gracePeriod;
    }
}
