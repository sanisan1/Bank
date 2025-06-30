package com.example.bank.model;

import java.math.BigDecimal;

public class TransferResponseDto {

        private Long fromUserId;
        private Long fromAccountId;
        private BigDecimal fromBalance;

        private Long toUserId;
        private Long toAccountId;
        private BigDecimal toBalance;

        private String comment;

    public TransferResponseDto(Long fromUserId, Long fromAccountId, BigDecimal fromBalance,
                               Long toUserId, Long toAccountId, BigDecimal toBalance, String comment) {
        this.fromUserId = fromUserId;
        this.fromAccountId = fromAccountId;
        this.fromBalance = fromBalance;
        this.toUserId = toUserId;
        this.toAccountId = toAccountId;
        this.toBalance = toBalance;
        this.comment = comment;
    }

    public TransferResponseDto() {

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getFromAccountId() {
            return fromAccountId;
        }

        public void setFromAccountId(Long fromAccountId) {
            this.fromAccountId = fromAccountId;
        }

        public BigDecimal getFromBalance() {
            return fromBalance;
        }

        public void setFromBalance(BigDecimal fromBalance) {
            this.fromBalance = fromBalance;
        }

        public Long getToAccountId() {
            return toAccountId;
        }

        public void setToAccountId(Long toAccountId) {
            this.toAccountId = toAccountId;
        }

        public BigDecimal getToBalance() {
            return toBalance;
        }

        public void setToBalance(BigDecimal toBalance) {
            this.toBalance = toBalance;
        }
    }


