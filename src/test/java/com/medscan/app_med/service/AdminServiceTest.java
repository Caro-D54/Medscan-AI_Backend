package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.DoseRepo;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import com.medscan.app_med.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private MedicamentRepo medicamentRepo;

    @Mock
    private TreatmentRepo treatmentRepo;

    @Mock
    private DoseRepo doseRepo;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepo, medicamentRepo, treatmentRepo, doseRepo);
    }

    @Test
    void getUsersWithoutQueryReturnsAllUsersPaged() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("one@medscan.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("two@medscan.com");

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(userRepo.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(user1, user2)));

        Page<UserDto> result = adminService.getUsers(null, pageRequest);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("User One");
        assertThat(result.getContent().get(1).email()).isEqualTo("two@medscan.com");
    }

    @Test
    void getUsersWithQuerySearchesByNameOrEmail() {
        User user = new User();
        user.setId(3L);
        user.setName("Admin User");
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(userRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("admin", "admin", pageRequest))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserDto> result = adminService.getUsers("admin", pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("admin@test.com");
    }

    @Test
    void deleteUserPreventsAdminFromDeletingSelf() {
        User currentAdmin = new User();
        currentAdmin.setId(99L);
        currentAdmin.setEmail("admin@test.com");

        assertThatThrownBy(() -> adminService.deleteUser(99L, currentAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No puedes eliminar tu propia cuenta");

        verify(userRepo, never()).delete(any());
    }

    @Test
    void deleteUserDeletesTargetUserWhenDifferent() {
        User currentAdmin = new User();
        currentAdmin.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("victim@medscan.com");

        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));

        adminService.deleteUser(2L, currentAdmin);

        verify(userRepo).delete(targetUser);
    }

    @Test
    void deleteUserThrows404WhenNotFound() {
        User currentAdmin = new User();
        currentAdmin.setId(1L);

        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(999L, currentAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void getMetricsCalculatesExpectedCounts() {
        when(userRepo.count()).thenReturn(10L);
        when(userRepo.countByRole(Role.ADMIN)).thenReturn(2L);
        when(userRepo.countByRole(Role.USER)).thenReturn(8L);
        when(userRepo.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(3L);
        when(medicamentRepo.count()).thenReturn(25L);
        when(treatmentRepo.count()).thenReturn(15L);
        when(doseRepo.count()).thenReturn(60L);

        AdminMetricsDto metrics = adminService.getMetrics();

        assertThat(metrics.totalUsers()).isEqualTo(10L);
        assertThat(metrics.adminUsers()).isEqualTo(2L);
        assertThat(metrics.regularUsers()).isEqualTo(8L);
        assertThat(metrics.totalMedications()).isEqualTo(25L);
        assertThat(metrics.totalTreatments()).isEqualTo(15L);
        assertThat(metrics.totalDoses()).isEqualTo(60L);
        assertThat(metrics.uptimeSeconds()).isGreaterThanOrEqualTo(0);
    }
}
