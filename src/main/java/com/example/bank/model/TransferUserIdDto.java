package com.example.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferUserIdDto {



    private Long fromAccouuntId;

    @NotNull
    private Long toUserId;

    @NotNull
    @Min(10)
    private BigDecimal amount;

    private String comment;

    public TransferUserIdDto(Long fromAccouuntId, Long toUserId, BigDecimal amount, String comment) {
        this.fromAccouuntId = fromAccouuntId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.comment = comment;
    }



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

    public @NotNull Long getFromAccId() {
        return fromAccouuntId;
    }

    public void setFromAccId(@NotNull Long fromAccouuntId) {
        this.fromAccouuntId = fromAccouuntId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
