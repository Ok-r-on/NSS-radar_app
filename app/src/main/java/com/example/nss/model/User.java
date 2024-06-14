package com.example.nss.model;

public class User {
    String name,email,pass;
    int hrs,status;

    public User(String username, String email, String pass) {
        this.name = username;
        this.email = email;
        this.pass = pass;
    }

    public User() {
    }

    public String getName() {
        return  name;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public int getHrs() {
        return hrs;
    }

    public int getStatus() {
        return status;
    }
}
