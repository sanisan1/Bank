package com.example.bank.mapper;

import com.example.bank.model.Account.Account;

import com.example.bank.model.Account.DebitAccount.DebitAccountResponse;
import org.springframework.stereotype.Component;

@Component
public class DebitAccountMapper {


    public static DebitAccountResponse toDto(Account account) {
        DebitAccountResponse dto = new DebitAccountResponse();
        dto.setBalance(account.getBalance());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getAccountType());
        return dto;
    }

}









