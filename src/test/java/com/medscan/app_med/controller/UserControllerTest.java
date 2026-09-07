package com.medscan.app_med.controller;

import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import com.medscan.app_med.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepo userRepo;

    @Test
    void updatePushTokenReturns204AndUpdatesToken() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/push-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pushToken": "ExponentPushToken[abcd1234]" }"""))
                .andExpect(status().isNoContent());

        verify(userService).updatePushToken("ExponentPushToken[abcd1234]");
    }

    @Test
    void updatePushTokenReturns400WhenTokenBlank() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/push-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pushToken": "" }"""))
                .andExpect(status().isBadRequest());
    }
}
