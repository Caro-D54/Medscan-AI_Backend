package com.medscan.app_med.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "medicaments")
@Data
public class Medicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String componentActive;

    @Size(max = 1000)
    @Column(length = 1000)
    private String secondaryEffect;

    private boolean withFood;

    @Size(max = 1000)
    @Column(length = 1000)
    private String dangerousInteractions;
}