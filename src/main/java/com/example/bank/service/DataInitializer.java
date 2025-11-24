package com.example.bank.service;

import com.example.bank.Enums.Role;

import com.example.bank.model.user.User;
import com.example.bank.repository.UserRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // если используешь Spring Security

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // шифруем пароль
            admin.setRole(Role.ADMIN);
            admin.setBlocked(false);
            admin.setEmail("admin@example.com");
            admin.setFirstName("System");
            admin.setLastName("Administrator");

            userRepository.save(admin);
            System.out.println("✅ Админ создан: admin / admin123");
        }
    }
}
