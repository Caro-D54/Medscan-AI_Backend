package com.medscan.app_med.service;

import com.medscan.app_med.model.User;

public record UserDto(Long id, String name, String email) {

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
