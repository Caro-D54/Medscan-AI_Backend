package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;

public record UserDto(Long id, String name, String email, Role role) {

    public UserDto(Long id, String name, String email) {
        this(id, name, email, Role.USER);
    }

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        Role userRole = user.getRole() != null ? user.getRole() : Role.USER;
        return new UserDto(user.getId(), user.getName(), user.getEmail(), userRole);
    }
}
