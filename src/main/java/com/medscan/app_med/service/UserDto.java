package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        Boolean isLoggedIn
) {

    public UserDto(Long id, String name, String email) {
        this(id, name, email, Role.USER, null, null, false);
    }

    public UserDto(Long id, String name, String email, Role role) {
        this(id, name, email, role, null, null, false);
    }

    public UserDto(Long id, String name, String email, Role role, LocalDateTime createdAt) {
        this(id, name, email, role, createdAt, null, false);
    }

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        Role userRole = user.getRole() != null ? user.getRole() : Role.USER;
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                userRole,
                user.getCreatedAt(),
                user.getLastLoginAt(),
                Boolean.TRUE.equals(user.getIsLoggedIn())
        );
    }
}
