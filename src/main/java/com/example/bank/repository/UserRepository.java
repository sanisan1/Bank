package com.example.bank.repository;


import com.example.bank.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccounts_Id(Long accountId);


    Optional<User> findByPhoneNumber(String fromPhone);

    Optional<User> findByUsername(String username);
}
