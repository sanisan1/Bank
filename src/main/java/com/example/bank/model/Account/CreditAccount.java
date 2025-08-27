package com.example.bank.model.Account;


import com.example.bank.model.Account.DebitAccount.Account;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CREDIT")
@SecondaryTable(name = "credit_accounts", pkJoinColumns = @PrimaryKeyJoinColumn(name = "account_id"))
public class CreditAccount extends Account {

    @Column(table = "credit_accounts")
    private BigDecimal creditLimit;

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
    @Override
    public String getAccountType() {
        return "DEBIT";
    }




}
