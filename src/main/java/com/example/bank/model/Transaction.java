package com.example.bank.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fromAccountId")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "fromUser")
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "toAccountId")
    private Account toAccount;

    @ManyToOne
    @JoinColumn(name = "toUser")

    private User toUser;
    private BigDecimal amount;
    private String comment;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;


    public Transaction(BigDecimal amount, User toUser, Account toAccount, LocalDateTime timestamp, Long id, User fromUser, Account fromAccount, String comment, OperationType operationType) {
        this.amount = amount;
        this.toUser = toUser;
        this.toAccount = toAccount;
        this.timestamp = timestamp;
        this.id = id;
        this.fromUser = fromUser;
        this.fromAccount = fromAccount;
        this.comment = comment;
        this.operationType = operationType;
    }

    public Transaction() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }
    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }


}
