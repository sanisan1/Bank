package com.example.bank.model.User;

import com.example.bank.model.Account.DebitAccount.DebitAccount;
import com.example.bank.Enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class UserDto {

    private Long userId;


    private String username;

    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private Role role;


    private LocalDateTime createdAt;


    private Boolean blocked;


    private List<DebitAccount> accounts = new ArrayList<>();

    public UserDto() {
        // пустой конструктор нужен для JPA
    }

    // Конструктор для удобства (без ролей, так как в вашем коде роли не используются)
    public UserDto(Long userId, Boolean blocked, LocalDateTime createdAt, String phoneNumber, String lastName, String firstName, String email, String password, String username, Role role) {
        this.userId = userId;
        this.blocked = blocked != null ? blocked : false; // если null, ставим false
        this.createdAt = createdAt;
        this.phoneNumber = phoneNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public List<DebitAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<DebitAccount> accounts) {
        this.accounts = accounts;
    }
}
