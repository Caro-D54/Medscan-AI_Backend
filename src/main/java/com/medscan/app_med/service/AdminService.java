package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.DoseRepo;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import com.medscan.app_med.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;

@Service
public class AdminService {

    private final UserRepo userRepo;
    private final MedicamentRepo medicamentRepo;
    private final TreatmentRepo treatmentRepo;
    private final DoseRepo doseRepo;

    public AdminService(UserRepo userRepo,
                        MedicamentRepo medicamentRepo,
                        TreatmentRepo treatmentRepo,
                        DoseRepo doseRepo) {
        this.userRepo = userRepo;
        this.medicamentRepo = medicamentRepo;
        this.treatmentRepo = treatmentRepo;
        this.doseRepo = doseRepo;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(String query, Pageable pageable) {
        return getUsers(query, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(String query, Boolean loggedIn, Pageable pageable) {
        if (Boolean.TRUE.equals(loggedIn)) {
            if (query != null && !query.trim().isEmpty()) {
                String sanitized = query.trim();
                return userRepo.findByIsLoggedInTrueAndSearch(sanitized, pageable)
                        .map(UserDto::fromEntity);
            }
            return userRepo.findByIsLoggedInTrue(pageable).map(UserDto::fromEntity);
        }

        if (query != null && !query.trim().isEmpty()) {
            String sanitized = query.trim();
            return userRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(sanitized, sanitized, pageable)
                    .map(UserDto::fromEntity);
        }
        return userRepo.findAll(pageable).map(UserDto::fromEntity);
    }

    @Transactional
    public void deleteUser(Long id, User currentAdmin) {
        if (currentAdmin == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User userPrincipal) {
                currentAdmin = userPrincipal;
            }
        }
        if (currentAdmin != null && currentAdmin.getId() != null && currentAdmin.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta de administrador");
        }

        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario con ID " + id + " no encontrado"));

        userRepo.delete(user);
    }

    @Transactional(readOnly = true)
    public AdminMetricsDto getMetrics() {
        long totalUsers = userRepo.count();
        long adminUsers = userRepo.countByRole(Role.SYSTEM);
        long regularUsers = userRepo.countByRole(Role.USER);
        long loggedInUsers = 0;
        try {
            loggedInUsers = userRepo.countByIsLoggedInTrue();
        } catch (Exception ignored) {
            // Fallback en caso de migración en curso
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newUsersLast7Days = userRepo.countByCreatedAtAfter(sevenDaysAgo);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsersLast30Days = userRepo.countByCreatedAtAfter(thirtyDaysAgo);

        long totalMedications = medicamentRepo.count();
        long totalTreatments = treatmentRepo.count();
        long totalDoses = doseRepo.count();

        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        return new AdminMetricsDto(
                totalUsers,
                regularUsers,
                adminUsers,
                loggedInUsers,
                newUsersLast7Days,
                newUsersLast30Days,
                totalMedications,
                totalTreatments,
                totalDoses,
                uptimeSeconds
        );
    }
}
