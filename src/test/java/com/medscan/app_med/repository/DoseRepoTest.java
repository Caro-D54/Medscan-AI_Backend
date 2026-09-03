package com.medscan.app_med.repository;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoseRepoTest {

    @Autowired
    private DoseRepo doseRepo;

    @Autowired
    private TreatmentRepo treatmentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MedicamentRepo medicamentRepo;

    private Treatment saveTreatment() {
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
        return treatmentRepo.save(treatment);
    }

    private Dose saveDose(Treatment treatment, DoseStatus status, LocalDateTime scheduledAt, LocalDateTime notifiedAt) {
        Dose dose = new Dose();
        dose.setTreatment(treatment);
        dose.setStatus(status);
        dose.setScheduledAt(scheduledAt);
        dose.setNotifiedAt(notifiedAt);
        return doseRepo.save(dose);
    }

    @Test
    void findDueToNotifyReturnsOnlyPendingDueNotYetNotifiedDoses() {
        Treatment treatment = saveTreatment();
        LocalDateTime deadline = LocalDateTime.of(2026, 9, 3, 12, 0);

        saveDose(treatment, DoseStatus.PENDING, deadline.minusMinutes(1), null);
        saveDose(treatment, DoseStatus.TAKEN, deadline.minusMinutes(1), null);
        saveDose(treatment, DoseStatus.PENDING, deadline.plusMinutes(1), null);
        saveDose(treatment, DoseStatus.PENDING, deadline.minusMinutes(1), LocalDateTime.of(2026, 9, 3, 9, 0));

        List<Dose> result = doseRepo.findDueToNotify(DoseStatus.PENDING, deadline);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(DoseStatus.PENDING);
        assertThat(result.get(0).getNotifiedAt()).isNull();
    }
}
