package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String name,
        Role role) {

    public RegisterRequest(String email, String password, String name) {
        this(email, password, name, null);
    }
}
