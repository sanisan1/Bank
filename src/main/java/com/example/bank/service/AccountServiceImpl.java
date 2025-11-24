package com.example.bank.service;

import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends AbstractAccountService {
    public AccountServiceImpl(AccountRepository accountRepository,
                              UserRepository userRepository,
                              AccountSecurity accountSecurity) {
        super(accountRepository, userRepository, accountSecurity);
    }
}
