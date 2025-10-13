package com.example.bank.model.Account;

import com.example.bank.model.AccountType;

import java.math.BigDecimal;

public class AccountDto {

    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
