package com.medscan.app_med.repository;

import com.medscan.app_med.model.Dose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoseRepo extends JpaRepository<Dose, Long> {
}
