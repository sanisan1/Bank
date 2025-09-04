package com.example.bank.mapper;


import com.example.bank.model.Account.CreditAccount;
import com.example.bank.model.Account.CreditAccountDto;

public class CreditAccountMapper {

    public static CreditAccountDto toDto(CreditAccount account) {
        CreditAccountDto dto = new CreditAccountDto();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setCreditLimit(account.getCreditLimit());
        dto.setDebt(account.getDebt());
        dto.setInterestRate(account.getInterestRate());
        dto.setMinimumPaymentRate(account.getMinimumPaymentRate());
        dto.setGracePeriod(account.getGracePeriod());
        dto.setPaymentDueDate(account.getPaymentDueDate());
        return dto;
    }

}
