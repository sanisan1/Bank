package com.example.bank.mapper;

import com.example.bank.model.Account;
import com.example.bank.model.AccountDto;
import com.example.bank.model.CreateAccountDto;
import com.example.bank.model.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {


    public static AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUserId(account.getUser().getUserId());
        dto.setBalance(account.getBalance());
        dto.setBlocked(account.getBlocked());
        return dto;
    }

    public static Account toEntity(CreateAccountDto dto, User user, long generatedId) {
        Account account = new Account();
        account.setId(generatedId);
        account.setUser(user);
        account.setBalance(dto.getBalance());
        account.setBlocked(dto.getBlocked());
        return account;
    }
}









