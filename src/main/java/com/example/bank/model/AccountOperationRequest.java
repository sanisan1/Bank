package com.example.bank.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AccountOperationRequest {

    @NotNull
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
