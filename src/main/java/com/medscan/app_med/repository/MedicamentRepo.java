package com.medscan.app_med.repository;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicamentRepo extends JpaRepository<Medicament, Long> {

    Page<Medicament> findByUser(User user, Pageable pageable);

    Page<Medicament> findByUserAndNameContainingIgnoreCase(User user, String name, Pageable pageable);

    Optional<Medicament> findByIdAndUser(Long id, User user);
}
