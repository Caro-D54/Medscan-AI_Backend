package com.medscan.app_med.security;

import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Test
    void registerEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"nuevo@medscan.com","password":"secret123","name":"Nuevo"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void protectedRouteWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/medications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteWithInvalidTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/medications")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedRouteWithValidTokenReturns200() throws Exception {
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");
        user = userRepo.save(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/medications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
