package com.example.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferAccountIdDto {

    private long fromAccId;
    @NotNull
    private long toAccId;
    @NotNull
    @Min(10)
    private BigDecimal amount;
    private String comment;
    public TransferAccountIdDto() {

    }

    public TransferAccountIdDto(BigDecimal amount, long toAccId, long fromAccId, String comment) {
        this.amount = amount;
        this.toAccId = toAccId;
        this.fromAccId = fromAccId;
        this.comment = comment;
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

    public long getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(long fromAccId) {
        this.fromAccId = fromAccId;
    }

    public long getToAccId() {
        return toAccId;
    }

    public void setToAccId(long toAccId) {
        this.toAccId = toAccId;
    }
}
