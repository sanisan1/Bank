package com.example.bank.controller;

import com.example.bank.model.User.CreateUserDto;
import com.example.bank.Enums.Role;
import com.example.bank.model.User.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {
    private final UserRepository userRepository;
    private final UserService userService;
    public RegistrationController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody CreateUserDto createUserDto) {

        createUserDto.setRole(Role.valueOf("USER"));
        return ResponseEntity.ok(userService.createUser(createUserDto));
    }


}
