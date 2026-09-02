package com.medscan.app_med.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medscan.app_med.service.AuthService;
import com.medscan.app_med.service.AuthResponse;
import com.medscan.app_med.service.DuplicateEmailException;
import com.medscan.app_med.service.InvalidCredentialsException;
import com.medscan.app_med.service.RegisterRequest;
import com.medscan.app_med.service.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void registerReturns201AndToken() throws Exception {
        AuthResponse response = new AuthResponse("jwt-token");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"caro@medscan.com","password":"secret123","name":"Carolina"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerWithDuplicateEmailReturns400() throws Exception {
        doThrow(new DuplicateEmailException("caro@medscan.com"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"caro@medscan.com","password":"secret123","name":"Carolina"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithBlankPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"caro@medscan.com","password":"","name":"Carolina"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns200AndToken() throws Exception {
        AuthResponse response = new AuthResponse("jwt-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"caro@medscan.com","password":"secret123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        doThrow(new InvalidCredentialsException())
                .when(authService).login(any(LoginRequest.class));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"caro@medscan.com","password":"wrong"}"""))
                .andExpect(status().isUnauthorized());
    }
}
