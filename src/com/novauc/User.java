package com.novauc;

public class User {
    private String name;
    private String password;
    private String interest;
    private int id;

    public User(String name, String password, String interest, int id) {
        this.name = name;
        this.password = password;
        this.interest = interest;
        this.id = id;
    }

    public User(String name, String password, String interest) {
        this.name = name;
        this.password = password;
        this.interest = interest;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
