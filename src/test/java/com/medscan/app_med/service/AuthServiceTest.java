package com.medscan.app_med.service;

import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerEncryptsPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("caro@medscan.com", "secret123", "Carolina");
        User saved = new User();
        saved.setId(1L);
        saved.setEmail("caro@medscan.com");
        saved.setPassword("hashed");
        saved.setName("Carolina");

        when(userRepo.existsByEmail("caro@medscan.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(userRepo).existsByEmail("caro@medscan.com");
        verify(passwordEncoder).encode("secret123");
        verify(userRepo).save(any(User.class));
        verify(jwtService).generateToken(saved);
    }

    @Test
    void registerWithDuplicateEmailThrowsDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest("caro@medscan.com", "secret123", "Carolina");

        when(userRepo.existsByEmail("caro@medscan.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void loginWithValidCredentialsReturnsToken() {
        LoginRequest request = new LoginRequest("caro@medscan.com", "secret123");
        User user = new User();
        user.setId(1L);
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");

        when(userRepo.findByEmail("caro@medscan.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void loginWithWrongPasswordThrowsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("caro@medscan.com", "wrong");
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");

        when(userRepo.findByEmail("caro@medscan.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithUnknownEmailThrowsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("ghost@medscan.com", "secret123");

        when(userRepo.findByEmail("ghost@medscan.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
