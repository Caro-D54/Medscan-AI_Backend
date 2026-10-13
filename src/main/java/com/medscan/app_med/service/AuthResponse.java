package com.medscan.app_med.service;

public class AuthResponse {

    private final String token;
    private final UserDto user;

    public AuthResponse(String token) {
        this(token, null);
    }

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserDto getUser() {
        return user;
    }
}
