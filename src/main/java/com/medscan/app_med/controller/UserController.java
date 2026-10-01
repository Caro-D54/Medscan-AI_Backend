package com.medscan.app_med.controller;

import com.medscan.app_med.service.PushTokenRequest;
import com.medscan.app_med.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.medscan.app_med.service.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping("/me/push-token")
    public ResponseEntity<Void> updatePushToken(@Valid @RequestBody PushTokenRequest request) {
        userService.updatePushToken(request.pushToken());
        return ResponseEntity.noContent().build();
    }
}
