package com.medscan.app_med.controller;

import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.service.AuthResponse;
import com.medscan.app_med.service.AuthService;
import com.medscan.app_med.service.LoginRequest;
import com.medscan.app_med.service.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepo userRepo;

    public AuthController(AuthService authService, UserRepo userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            user.setIsLoggedIn(false);
            userRepo.save(user);
        }
        return ResponseEntity.noContent().build();
    }
}
