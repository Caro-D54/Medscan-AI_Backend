package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(Role.USER);
        user.setLastLoginAt(LocalDateTime.now());
        user.setIsLoggedIn(true);
        User saved = userRepo.save(user);

        return new AuthResponse(jwtService.generateToken(saved), UserDto.fromEntity(saved));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setIsLoggedIn(true);
        User saved = userRepo.save(user);

        return new AuthResponse(jwtService.generateToken(saved), UserDto.fromEntity(saved));
    }
}
