package com.medscan.app_med.repository;

import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TreatmentRepo extends JpaRepository<Treatment, Long> {

    @Query("SELECT DISTINCT t FROM Treatment t JOIN FETCH t.medicament LEFT JOIN FETCH t.doses WHERE t.user = :user AND t.endDate >= :today ORDER BY t.startDate ASC, t.id ASC")
    List<Treatment> findActiveByUser(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT DISTINCT t FROM Treatment t JOIN FETCH t.medicament LEFT JOIN FETCH t.doses WHERE t.user = :user ORDER BY t.startDate ASC, t.id ASC")
    List<Treatment> findAllByUser(@Param("user") User user);
}
