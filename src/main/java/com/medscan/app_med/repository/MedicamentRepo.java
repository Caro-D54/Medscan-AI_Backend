package com.medscan.app_med.repository;

import com.medscan.app_med.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentRepo extends JpaRepository<Medicament, Long> {
    
    String findByName(String name);
    
}
