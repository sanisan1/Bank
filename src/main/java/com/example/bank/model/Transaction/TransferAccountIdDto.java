package com.example.bank.model.Transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferAccountIdDto {

    private String fromAccountNumber;
    @NotNull
    private String toAccountNumber;
    @NotNull
    @Min(10)
    private BigDecimal amount;
    private String comment;

    public TransferAccountIdDto() {

    }

    public TransferAccountIdDto(String fromAccountNumber, String comment, BigDecimal amount, String toAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
        this.comment = comment;
        this.amount = amount;
        this.toAccountNumber = toAccountNumber;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }
}