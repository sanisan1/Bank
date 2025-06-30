package com.example.bank.model;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountDto {
    @NotNull
    private Long userId;
    private BigDecimal balance;
    private Boolean blocked;



    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }
}
