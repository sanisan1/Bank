package com.example.bank.exception;

import com.example.bank.model.account.Account;


public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(Account account) {
        super("Operation failed user: " + account.getAccountNumber() + " is blocked");
    }
}
