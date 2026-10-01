package com.medscan.app_med.service;

import com.medscan.app_med.model.User;

public record UserResponse(Long id, String name, String email) {

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
