package com.example.fyp1;

public class User {
    private String name;
    private String phone;
    private String email;
    private String balance;

    public User(){ };

    public User(String name, String phone, String email, String balance) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() { return email; }

    public String getBalance() {
        return balance;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
