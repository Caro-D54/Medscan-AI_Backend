package com.medscan.app_med.repository;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoseRepo extends JpaRepository<Dose, Long> {

    @Query("SELECT d FROM Dose d WHERE d.status = :status AND d.notifiedAt IS NULL AND d.scheduledAt <= :deadline")
    List<Dose> findDueToNotify(@Param("status") DoseStatus status,
                               @Param("deadline") LocalDateTime deadline);
}
