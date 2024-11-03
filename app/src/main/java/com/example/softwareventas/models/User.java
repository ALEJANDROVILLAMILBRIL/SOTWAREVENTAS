package com.example.softwareventas.models;

public class User
{
    public String username, email, address, phone, birthdate, role;

    public User()
    {

    }

    public User(String username, String email, String address, String phone, String birthdate, String role) {
        this.username = username;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.birthdate = birthdate;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}