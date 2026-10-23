package com.medscan.app_med.config;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin_user@test.com}")
    private String adminEmail;

    @Value("${app.admin.password:12345_admin}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador del Sistema}")
    private String adminName;

    public AdminSeeder(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepo.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    boolean updated = false;
                    if (user.getRole() != Role.SYSTEM) {
                        user.setRole(Role.SYSTEM);
                        updated = true;
                    }
                    if (!passwordEncoder.matches(adminPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(adminPassword));
                        updated = true;
                    }
                    if (updated) {
                        userRepo.save(user);
                        log.info("Usuario existente actualizado a rol SYSTEM con credenciales configuradas: {}", adminEmail);
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setEmail(adminEmail);
                    admin.setName(adminName);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(Role.SYSTEM);
                    userRepo.save(admin);
                    log.info("Usuario Administrador inicial creado exitosamente: {}", adminEmail);
                }
        );
    }
}
