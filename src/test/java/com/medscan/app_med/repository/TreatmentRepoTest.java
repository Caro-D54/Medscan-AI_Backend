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

    @Test
    void findActiveByUserReturnsOnlyTreatmentsWithEndDateOnOrAfterToday() {
        LocalDate today = LocalDate.of(2026, 10, 6);

        User user = new User();
        user.setEmail("active@medscan.com");
        user.setPassword("hashed");
        user.setName("Active User");
        user = userRepo.save(user);

        Medicament med = new Medicament();
        med.setName("Amoxicilina");
        med.setUser(user);
        med = medicamentRepo.save(med);

        Treatment active = new Treatment();
        active.setUser(user);
        active.setMedicament(med);
        active.setStartDate(today.minusDays(1));
        active.setEndDate(today.plusDays(3));
        active.setIntervalHours(8);
        active.setDoseQuantity(1);
        active.setStartTime(LocalTime.of(8, 0));

        Dose doseActive = new Dose();
        doseActive.setTreatment(active);
        doseActive.setScheduledAt(LocalDateTime.of(today, LocalTime.of(8, 0)));
        doseActive.setStatus(DoseStatus.PENDING);
        active.getDoses().add(doseActive);
        treatmentRepo.save(active);

        Treatment expired = new Treatment();
        expired.setUser(user);
        expired.setMedicament(med);
        expired.setStartDate(today.minusDays(5));
        expired.setEndDate(today.minusDays(1));
        expired.setIntervalHours(12);
        expired.setDoseQuantity(1);
        expired.setStartTime(LocalTime.of(8, 0));
        treatmentRepo.save(expired);

        List<Treatment> activeResults = treatmentRepo.findActiveByUser(user, today);

        assertThat(activeResults).hasSize(1);
        assertThat(activeResults.get(0).getId()).isEqualTo(active.getId());
        assertThat(activeResults.get(0).getMedicationName()).isEqualTo("Amoxicilina");
        assertThat(activeResults.get(0).getDoses()).hasSize(1);

        List<Treatment> allResults = treatmentRepo.findAllByUser(user);
        assertThat(allResults).hasSize(2);
    }

    @Test
    void findActiveByUserDoesNotReturnOtherUsersTreatments() {
        LocalDate today = LocalDate.of(2026, 10, 6);

        User owner = new User();
        owner.setEmail("user1@medscan.com");
        owner.setPassword("hashed");
        owner.setName("User 1");
        owner = userRepo.save(owner);

        User other = new User();
        other.setEmail("user2@medscan.com");
        other.setPassword("hashed");
        other.setName("User 2");
        other = userRepo.save(other);

        Medicament med = new Medicament();
        med.setName("Ibuprofeno");
        med.setUser(owner);
        med = medicamentRepo.save(med);

        Treatment treatment = new Treatment();
        treatment.setUser(owner);
        treatment.setMedicament(med);
        treatment.setStartDate(today);
        treatment.setEndDate(today.plusDays(2));
        treatment.setIntervalHours(8);
        treatment.setDoseQuantity(1);
        treatment.setStartTime(LocalTime.of(8, 0));
        treatmentRepo.save(treatment);

        List<Treatment> results = treatmentRepo.findActiveByUser(other, today);

        assertThat(results).isEmpty();
    }
}
