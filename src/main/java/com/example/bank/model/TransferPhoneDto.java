package com.example.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferPhoneDto {

    private long fromAccId;
    @NotBlank
    private String phone;
    @NotNull
    @Min(10)
    private BigDecimal amount;
    private String comment;
    public TransferPhoneDto() {

    }

    public TransferPhoneDto(BigDecimal amount, String phone, long fromAccId, String comment) {
        this.amount = amount;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone() {
        this.phone = phone;
    }
}
