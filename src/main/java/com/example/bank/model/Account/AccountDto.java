package com.example.bank.model.Account;

import com.example.bank.Enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class AccountDto {

    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;

}