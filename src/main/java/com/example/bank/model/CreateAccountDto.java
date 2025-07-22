package com.example.bank.model;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountDto {

    private BigDecimal balance;
    private Boolean blocked;





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
