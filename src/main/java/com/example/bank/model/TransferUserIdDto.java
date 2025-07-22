package com.example.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferUserIdDto {



    private Long fromAccId;

    @NotNull
    private Long toUserId;

    @NotNull
    @Min(10)
    private BigDecimal amount;

    private String comment;



    public TransferUserIdDto() {
    }

    public @NotNull @Min(10) BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull @Min(10) BigDecimal amount) {
        this.amount = amount;
    }

    public @NotNull Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(@NotNull Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(Long fromAccId) {
        this.fromAccId = fromAccId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
