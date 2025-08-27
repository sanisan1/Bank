package com.example.bank.mapper;

import com.example.bank.model.Account.DebitAccount.AccountDto;
import com.example.bank.model.Account.DebitAccount.DebitAccount;

import com.example.bank.model.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {


    public static AccountDto toDto(DebitAccount account) {
        AccountDto dto = new AccountDto();
        dto.setBalance(account.getBalance());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getAccountType());
        return dto;
    }

}









