package com.medscan.app_med.repository;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentRepo extends JpaRepository<Medicament, Long> {

    List<Medicament> findByUserOrderByIdAsc(User user);

    List<Medicament> findByUserAndNameContainingIgnoreCaseOrderByIdAsc(User user, String name);

    Optional<Medicament> findByIdAndUser(Long id, User user);
}
