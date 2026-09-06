package com.medscan.app_med.repository;

import com.medscan.app_med.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicamentRepo extends JpaRepository<Medicament, Long> {

    Optional<Medicament> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}