package com.example.bank.service;

import com.example.bank.model.account.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl extends AbstractAccountService {

    public AccountServiceImpl(AccountRepository accountRepository,
                              UserRepository userRepository,
                              AccountSecurity accountSecurity) {
        super(accountRepository, userRepository, accountSecurity);
    }


    public List<Account> getAllAccounts() {
        if (accountRepository.findByUser_UserId(getCurrentUser().getUserId()).isEmpty()) {
            return List.of();
        }

        return accountRepository.findByUser_UserId(getCurrentUser().getUserId());

    }
}
