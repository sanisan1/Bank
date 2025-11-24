package com.example.bank.model.Account.DebitAccount;

import com.example.bank.model.Account.Account;
import com.example.bank.Enums.AccountType;
import jakarta.persistence.*;
@Entity
public class DebitAccount extends Account {
    public DebitAccount() { super();
        setAccountType(AccountType.DEBIT);
    }



}
