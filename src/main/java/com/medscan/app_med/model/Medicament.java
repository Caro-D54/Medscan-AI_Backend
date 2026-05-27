package com.medscan.app_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "medicaments")
@Data
public class Medicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String ComponentActive;

    @Column(length = 1000)
        private String SecondaryEffect;

        private boolean withFood;

    @Column(length = 1000)
        private String DangerousInteractions;
    
    
}
