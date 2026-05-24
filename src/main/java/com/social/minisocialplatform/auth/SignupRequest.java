package com.social.minisocialplatform.auth;

public class SignupRequest {
    private String username;
    private String password;
    private String role;

    public SignupRequest() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}