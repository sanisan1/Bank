package com.example.bank.model.account.debitAccount;

import com.example.bank.model.account.Account;
import com.example.bank.Enums.AccountType;
import jakarta.persistence.*;
@Entity
public class DebitAccount extends Account {
    public DebitAccount() { super();
        setAccountType(AccountType.DEBIT);
    }



}
