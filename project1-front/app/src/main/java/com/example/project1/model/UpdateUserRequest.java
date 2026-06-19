package com.example.project1.model;

public class UpdateUserRequest {
    public String name;
    public Integer age;
    public String occupation;
    public String gender;

    public UpdateUserRequest(String name, Integer age, String occupation, String gender) {
        this.name = name;
        this.age = age;
        this.occupation = occupation;
        this.gender = gender;
    }
}
