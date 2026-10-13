package com.medscan.app_med.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    void constructorWithTokenAndUserSetsBothProperties() {
        UserDto user = new UserDto(1L, "Carolina", "caro@medscan.com");
        AuthResponse response = new AuthResponse("sample-jwt-token", user);

        assertThat(response.getToken()).isEqualTo("sample-jwt-token");
        assertThat(response.getUser()).isEqualTo(user);
    }

    @Test
    void constructorWithTokenOnlySetsNullUser() {
        AuthResponse response = new AuthResponse("sample-jwt-token");

        assertThat(response.getToken()).isEqualTo("sample-jwt-token");
        assertThat(response.getUser()).isNull();
    }
}
