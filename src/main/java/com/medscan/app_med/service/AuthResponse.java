package com.medscan.app_med.service;

public class AuthResponse {

    private final String token;
    private final UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public AuthResponse(String token) {
        this(token, null);
    }

    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }
}
