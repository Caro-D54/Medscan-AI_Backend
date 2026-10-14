package com.medscan.app_med.controller;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin_user@test.com");
        adminUser.setPassword("hashed");
        adminUser.setName("Admin Tester");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepo.save(adminUser);

        regularUser = new User();
        regularUser.setEmail("regular@medscan.com");
        regularUser.setPassword("hashed");
        regularUser.setName("Regular Tester");
        regularUser.setRole(Role.USER);
        regularUser = userRepo.save(regularUser);

        adminToken = jwtService.generateToken(adminUser);
        userToken = jwtService.generateToken(regularUser);
    }

    @Test
    void adminEndpointsWithoutTokenReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/admin/users/" + regularUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserAccessingAdminEndpointsReturns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/metrics")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListUsersWithPaginationAndSearch() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "regular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("regular@medscan.com"));
    }

    @Test
    void adminCanGetSystemMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.adminUsers").value(1))
                .andExpect(jsonPath("$.regularUsers").value(1))
                .andExpect(jsonPath("$.uptimeSeconds").isNumber());
    }

    @Test
    void adminCannotDeleteSelfReturns400() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanDeleteOtherUserReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
