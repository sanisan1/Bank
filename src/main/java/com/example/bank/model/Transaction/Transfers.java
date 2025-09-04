package com.example.bank.model.Transaction;

import com.example.bank.model.Account.DebitAccount.Account;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.OperationType;
import com.example.bank.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transfers")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Transfers extends Transaction {

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;

    private String comment;

    public Transfers() {}

    public Transfers(User fromUser, DebitAccount fromAccount,
                     User toUser, DebitAccount toAccount,
                     BigDecimal amount, String comment) {
        setOperationType(OperationType.transfer);
        setAmount(amount);

        this.fromUser = fromUser;
        this.fromAccount = fromAccount;
        this.toUser = toUser;
        this.toAccount = toAccount;
        this.comment = comment;
    }

    // Геттеры и сеттеры
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
