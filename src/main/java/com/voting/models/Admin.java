package com.voting.models;

public class Admin extends User {
    public Admin(String id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public String getRole() { return "ADMIN"; }
}
