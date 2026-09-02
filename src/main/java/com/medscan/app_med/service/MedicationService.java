package com.medscan.app_med.service;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.MedicamentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MedicationService {

    private final MedicamentRepo medicamentRepo;

    public MedicationService(MedicamentRepo medicamentRepo) {
        this.medicamentRepo = medicamentRepo;
    }

    public Page<Medicament> getMedications(String nameFilter, Pageable pageable) {
        User user = currentUser();
        if (nameFilter == null || nameFilter.isBlank()) {
            return medicamentRepo.findByUser(user, pageable);
        }
        return medicamentRepo.findByUserAndNameContainingIgnoreCase(user, nameFilter.trim(), pageable);
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
        return medicamentRepo.save(existing);
    }

    public void deleteMedication(Long id) {
        Medicament existing = medicamentRepo.findByIdAndUser(id, currentUser())
                .orElseThrow(MedicationNotFoundException::new);
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
