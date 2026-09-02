package com.medscan.app_med.service;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.MedicamentRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

    private final MedicamentRepo medicamentRepo;

    public MedicationService(MedicamentRepo medicamentRepo) {
        this.medicamentRepo = medicamentRepo;
    }

    public List<Medicament> getMedications(String nameFilter) {
        User user = currentUser();
        if (nameFilter == null || nameFilter.isBlank()) {
            return medicamentRepo.findByUserOrderByIdAsc(user);
        }
        return medicamentRepo.findByUserAndNameContainingIgnoreCaseOrderByIdAsc(user, nameFilter.trim());
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
