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

    private void scheduleDoses(Treatment treatment) {
        List<LocalTime> dailyTimes = dailyTimes(treatment.getStartTime(), treatment.getIntervalHours());
        LocalDate day = treatment.getStartDate();
        while (!day.isAfter(treatment.getEndDate())) {
            for (LocalTime time : dailyTimes) {
                Dose dose = new Dose();
                dose.setTreatment(treatment);
                dose.setScheduledAt(LocalDateTime.of(day, time));
                dose.setStatus(DoseStatus.PENDING);
                treatment.getDoses().add(dose);
            }
            day = day.plusDays(1);
        }
    }

    private List<LocalTime> dailyTimes(LocalTime startTime, int intervalHours) {
        List<LocalTime> times = new ArrayList<>();
        int offset = 0;
        while (offset < 24) {
            times.add(startTime.plusHours(offset));
            offset += intervalHours;
        }
        return times;
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user");
    }
}
