package com.example.matha.medwayhealth.Model;

/**
 * Created by sickbay on 12/13/2017.
 */

public class User {
    private String phone;
    private String password;
    private String email;
    private String name;



    public User(String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.name = name;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

}
