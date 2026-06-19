package com.example.project1.model;

public class RegisterRequest {
    public String phone;
    public String password;
    public String name;
    public Integer age;
    public String occupation;
    public String gender;

    public RegisterRequest(
            String phone,
            String password,
            String name,
            Integer age,
            String occupation,
            String gender) {
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.age = age;
        this.occupation = occupation;
        this.gender = gender;
    }
}
