package com.example.bank.model.Account.DebitAccount;

import com.example.bank.Enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class DebitAccountResponse {

    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;

}
