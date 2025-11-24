package com.example.bank.model.user;

import com.example.bank.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;


    private String username;

    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;


    private Role role;


    private LocalDateTime createdAt;


    private Boolean blocked;


}
