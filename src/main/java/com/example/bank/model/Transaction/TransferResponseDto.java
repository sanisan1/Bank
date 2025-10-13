package com.example.bank.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponseDto {
    private Long id;
    private BigDecimal amount;
    private String operationType;
    private LocalDateTime time;

    private Long fromAccountId;
    private Long fromUserId;
    private Long toAccountId;
    private Long toUserId;

    private String comment;

    // геттеры/сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
