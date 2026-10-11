package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.DoseRepo;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TreatmentService {

    private final TreatmentRepo treatmentRepo;
    private final DoseRepo doseRepo;
    private final MedicamentRepo medicamentRepo;

    public TreatmentService(TreatmentRepo treatmentRepo,
                            DoseRepo doseRepo,
                            MedicamentRepo medicamentRepo) {
        this.treatmentRepo = treatmentRepo;
        this.doseRepo = doseRepo;
        this.medicamentRepo = medicamentRepo;
    }

    @Transactional
    public Treatment createTreatment(CreateTreatmentRequest request) {
        User user = currentUser();
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidTreatmentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days > 365) {
            throw new InvalidTreatmentException("La duración del tratamiento no puede exceder los 365 días");
        }
        if (request.intervalHours() <= 0) {
            throw new InvalidTreatmentException("El intervalo entre dosis debe ser de al menos 1 hora");
        }
        if (request.intervalHours() > 168) {
            throw new InvalidTreatmentException("El intervalo entre dosis no puede superar 168 horas (1 semana)");
        }
        if (request.doseQuantity() <= 0) {
            throw new InvalidTreatmentException("La cantidad por dosis debe ser mayor a cero");
        }

        Medicament medicament = medicamentRepo.findByIdAndUser(request.medicationId(), user)
                .orElseThrow(MedicationNotFoundException::new);

        Treatment treatment = new Treatment();
        treatment.setMedicament(medicament);
        treatment.setUser(user);
        treatment.setStartDate(request.startDate());
        treatment.setEndDate(request.endDate());
        treatment.setIntervalHours(request.intervalHours());
        treatment.setDoseQuantity(request.doseQuantity());
        treatment.setStartTime(request.startTime());
        scheduleDoses(treatment);

        return treatmentRepo.save(treatment);
    }

    @Transactional
    public Dose markDose(Long doseId, DoseStatus status) {
        User user = currentUser();
        Dose dose = doseRepo.findById(doseId).orElseThrow(DoseNotFoundException::new);
        if (!dose.getTreatment().getUser().getId().equals(user.getId())) {
            throw new DoseNotFoundException();
        }
        dose.setStatus(status);
        return doseRepo.save(dose);
    }

    @Transactional(readOnly = true)
    public List<Treatment> getActiveTreatments() {
        return getTreatments(true);
    }

    @Transactional(readOnly = true)
    public List<Treatment> getTreatments(boolean activeOnly) {
        User user = currentUser();
        if (activeOnly) {
            return treatmentRepo.findActiveByUser(user, LocalDate.now());
        }
        return treatmentRepo.findAllByUser(user);
    }

    private void scheduleDoses(Treatment treatment) {
        long totalDays = ChronoUnit.DAYS.between(treatment.getStartDate(), treatment.getEndDate()) + 1;
        LocalDateTime current = LocalDateTime.of(treatment.getStartDate(), treatment.getStartTime());
        LocalDateTime endLimit = current.plusDays(totalDays);

        while (current.isBefore(endLimit)) {
            Dose dose = new Dose();
            dose.setTreatment(treatment);
            dose.setScheduledAt(current);
            dose.setStatus(DoseStatus.PENDING);
            treatment.getDoses().add(dose);
            current = current.plusHours(treatment.getIntervalHours());
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user");
    }
}
