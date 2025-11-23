package com.example.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор уведомления
    @NotNull
    private Long userId; // кому принадлежит уведомление
    @Enumerated(EnumType.STRING)
    private NotflicationType type; // тип операции: WITHDRAW, DEPOSIT, TRANSFER, FRAUD, INFO
    @NotNull
    private String title; // короткое описание ("Снятие средств", "Зачисление", "Перевод")

    private String accountNumber;

    private String comment;
    @NotNull
    private String message; // более подробное сообщение ("Со cчета ****1234 снято 1000 ₽")

    private Boolean read = false; // отмечено как прочитано

    private LocalDateTime createdAt = LocalDateTime.now();

    private BigDecimal amount;
 
    // можно добавить: externalReference/transactionId для связи с транзакциями
    private Long referenceId;

    public Notification(Long id, Long userId, NotflicationType type, String title, Boolean read, String message, LocalDateTime createdAt, Long referenceId) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.read = read;
        this.message = message;
        this.createdAt = createdAt;
        this.referenceId = referenceId;
    }

    public Notification() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NotflicationType getType() {
        return type;
    }

    public void setType(NotflicationType type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
