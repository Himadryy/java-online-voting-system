package com.voting.models;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String id;
    private String name;
    private String password;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }

    public abstract String getRole();
}
