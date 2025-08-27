package com.example.bank.model.Transaction;


import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.OperationType;
import com.example.bank.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Payment extends Transaction {

    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false)
    private DebitAccount fromAccount;  // С какого счёта списываем

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    private String recipient;

    private String description;
    private String toAccountNumber;

    public Payment(User fromUser, DebitAccount fromAccount,
                   BigDecimal amount, String recipient, String description, String toAccountNumber) {
        setAmount(amount);
        this.fromUser = fromUser;
        this.fromAccount = fromAccount;
        this.recipient = recipient;
        this.description = description;
        this.setOperationType(OperationType.payment);
        this.toAccountNumber = toAccountNumber;
    }

    public Payment() {

    }


    // Геттеры и сеттеры
    public DebitAccount getFromAccount() { return fromAccount; }
    public void setFromAccount(DebitAccount fromAccount) { this.fromAccount = fromAccount; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


}
