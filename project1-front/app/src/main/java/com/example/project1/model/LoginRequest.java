package com.example.project1.model;

public class LoginRequest {
    public String phone;
    public String password;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}