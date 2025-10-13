package com.example.bank.mapper;

import com.example.bank.model.Account.CreditAccount.CreditAccount;
import com.example.bank.model.Account.CreditAccount.CreditAccountResponseDto;

public class CreditAccountMapper {

    // Преобразование из CreditAccount в DTO
    public static CreditAccountResponseDto toDto(CreditAccount creditAccount) {
        if (creditAccount == null) {
            return null;
        }

        return new CreditAccountResponseDto(
                creditAccount.getAccountNumber(),
                creditAccount.getBalance(),
                creditAccount.getCreditLimit(),
                creditAccount.getInterestRate(),
                creditAccount.getMinimumPaymentRate(),
                creditAccount.getGracePeriod(),
                creditAccount.getTotalDebt(), // долг
                creditAccount.getPaymentDueDate()
        );
    }
}
