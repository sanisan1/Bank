package com.example.bank.model.User;

import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
