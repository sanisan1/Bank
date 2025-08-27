package com.example.bank.model.Account.DebitAccount;

import jakarta.persistence.*;
@Entity
@DiscriminatorValue("DEBIT")
public class DebitAccount extends Account {
    public DebitAccount() { super(); }

    @Override
    public String getAccountType() {
        return "DEBIT";
    }
}
