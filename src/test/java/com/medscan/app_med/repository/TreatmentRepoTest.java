package com.medscan.app_med.repository;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TreatmentRepoTest {

    @Autowired
    private TreatmentRepo treatmentRepo;

    @Autowired
    private DoseRepo doseRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MedicamentRepo medicamentRepo;

    @Test
    void saveTreatmentCascadesPersistingItsDoses() {
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");
        user = userRepo.save(user);

        Medicament medicament = new Medicament();
        medicament.setName("Acetaminophen");
        medicament.setUser(user);
        medicament = medicamentRepo.save(medicament);

        Treatment treatment = new Treatment();
        treatment.setUser(user);
        treatment.setMedicament(medicament);
        treatment.setStartDate(LocalDate.of(2026, 9, 1));
        treatment.setEndDate(LocalDate.of(2026, 9, 2));
        treatment.setIntervalHours(8);
        treatment.setDoseQuantity(1);
        treatment.setStartTime(LocalTime.of(8, 0));

        Dose first = new Dose();
        first.setTreatment(treatment);
        first.setScheduledAt(LocalDateTime.of(2026, 9, 1, 8, 0));
        first.setStatus(DoseStatus.PENDING);
        treatment.getDoses().add(first);

        treatment = treatmentRepo.save(treatment);

        List<Dose> persisted = doseRepo.findAll();
        assertThat(persisted).hasSize(1);
        assertThat(persisted.get(0).getTreatment().getId()).isEqualTo(treatment.getId());
        assertThat(persisted.get(0).getStatus()).isEqualTo(DoseStatus.PENDING);
    }
}
