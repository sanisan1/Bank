package com.example.bank.model.Account.DebitAccount;

import com.example.bank.model.AccountType;
import jakarta.validation.constraints.NotNull;
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
