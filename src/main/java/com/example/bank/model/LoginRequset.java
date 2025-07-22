package com.example.bank.model;

public class LoginRequset {
    public LoginRequset(String password, String username) {
        this.password = password;
        this.username = username;
    }

    private String username;
    private String password;

    public LoginRequset() {

    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }




}
