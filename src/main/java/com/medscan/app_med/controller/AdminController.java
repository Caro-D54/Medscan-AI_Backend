package com.medscan.app_med.controller;

import com.medscan.app_med.model.User;
import com.medscan.app_med.service.AdminMetricsDto;
import com.medscan.app_med.service.AdminService;
import com.medscan.app_med.service.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@PreAuthorize("hasRole('SYSTEM')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean loggedIn,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        String searchTerm = query != null ? query : search;
        return ResponseEntity.ok(adminService.getUsers(searchTerm, loggedIn, pageable));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {

        adminService.deleteUser(id, currentAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDto> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }
}
