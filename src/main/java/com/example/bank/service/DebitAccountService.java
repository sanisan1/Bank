package com.example.bank.service;

import com.example.bank.exception.InvalidOperationException;
import com.example.bank.mapper.DebitAccountMapper;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.model.Account.DebitAccount.DebitAccountResponse;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.AccountSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DebitAccountService extends AbstractAccountService {

    private static final Logger log = LoggerFactory.getLogger(DebitAccountService.class);

    public DebitAccountService(AccountRepository accountRepository,
                               UserRepository userRepository,
                               AccountSecurity accountSecurity) {
        super(accountRepository, userRepository, accountSecurity);
    }

    // Create debit account for the current user
    public DebitAccountResponse createAccount() {
        log.info("Creating debit account");
        try {
            var user = getCurrentUser();

            DebitAccount account = new DebitAccount();
            account.setUser(user);
            account.setAccountNumber(generateUniqueAccountNumber());
            DebitAccount saved = accountRepository.save(account);

            if (user.getMainAccount() == null) {
                user.setMainAccount(saved);
                log.info("Assigned as main account: {}", saved.getAccountNumber());
            }

            return DebitAccountMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error creating debit account: {}", e.getMessage(), e);
            throw e;
        }
    }
}
