package com.medscan.app_med.service;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicationService {

    private final MedicamentRepo medicamentRepo;
    private final TreatmentRepo treatmentRepo;

    public MedicationService(MedicamentRepo medicamentRepo, TreatmentRepo treatmentRepo) {
        this.medicamentRepo = medicamentRepo;
        this.treatmentRepo = treatmentRepo;
    }

    public Page<Medicament> getMedications(String nameFilter, Pageable pageable) {
        User user = currentUser();
        if (nameFilter == null || nameFilter.isBlank()) {
            return medicamentRepo.findByUser(user, pageable);
        }
        return medicamentRepo.findByUserAndNameContainingIgnoreCase(user, nameFilter.trim(), pageable);
    }

    public Medicament getMedicationById(Long id) {
        return medicamentRepo.findByIdAndUser(id, currentUser())
                .orElseThrow(MedicationNotFoundException::new);
    }

    public Medicament createMedication(Medicament medicament) {
        medicament.setId(null);
        medicament.setUser(currentUser());
        return medicamentRepo.save(medicament);
    }

    public Medicament updateMedication(Long id, Medicament medicament) {
        Medicament existing = medicamentRepo.findByIdAndUser(id, currentUser())
                .orElseThrow(MedicationNotFoundException::new);
        existing.setName(medicament.getName());
        existing.setComponentActive(medicament.getComponentActive());
        existing.setSecondaryEffect(medicament.getSecondaryEffect());
        existing.setWithFood(medicament.isWithFood());
        existing.setDangerousInteractions(medicament.getDangerousInteractions());
        existing.setDosage(medicament.getDosage());
        existing.setFrequency(medicament.getFrequency());
        existing.setInstructions(medicament.getInstructions());
        return medicamentRepo.save(existing);
    }

    @Transactional
    public void deleteMedication(Long id) {
        Medicament existing = medicamentRepo.findByIdAndUser(id, currentUser())
                .orElseThrow(MedicationNotFoundException::new);
        List<Treatment> treatments = treatmentRepo.findByMedicament(existing);
        treatmentRepo.deleteAll(treatments);
        medicamentRepo.delete(existing);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user");
    }
}
