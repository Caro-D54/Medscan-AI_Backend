package com.medscan.app_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String pushToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    private java.time.LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private Boolean isLoggedIn = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
        if (isLoggedIn == null) {
            isLoggedIn = false;
        }
    }
}
