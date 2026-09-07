package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.DoseRepo;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock
    private TreatmentRepo treatmentRepo;

    @Mock
    private DoseRepo doseRepo;

    @Mock
    private MedicamentRepo medicamentRepo;

    @InjectMocks
    private TreatmentService treatmentService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("caro@medscan.com");
        currentUser.setPassword("hashed");
        currentUser.setName("Carolina");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateTreatmentRequest request(int intervalHours) {
        return new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                intervalHours,
                1,
                LocalTime.of(8, 0));
    }

    private Medicament ownedMedicament() {
        Medicament medicament = new Medicament();
        medicament.setId(5L);
        medicament.setName("Acetaminophen");
        medicament.setUser(currentUser);
        return medicament;
    }

    @Test
    void createTreatmentGeneratesScheduleAcrossTheWholeRange() {
        when(medicamentRepo.findByIdAndUser(5L, currentUser)).thenReturn(Optional.of(ownedMedicament()));
        when(treatmentRepo.save(org.mockito.ArgumentMatchers.any(Treatment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        treatmentService.createTreatment(request(8));

        ArgumentCaptor<Treatment> captor = ArgumentCaptor.forClass(Treatment.class);
        verify(treatmentRepo).save(captor.capture());
        Treatment saved = captor.getValue();

        assertThat(saved.getMedicationId()).isEqualTo(5L);
        assertThat(saved.getDoseQuantity()).isEqualTo(1);
        assertThat(saved.getIntervalHours()).isEqualTo(8);
        assertThat(saved.getDoses()).hasSize(6);

        List<LocalTime> times = saved.getDoses().stream()
                .map(Dose::getScheduledAt)
                .map(java.time.LocalDateTime::toLocalTime)
                .toList();
        assertThat(times).containsExactly(
                LocalTime.of(8, 0), LocalTime.of(16, 0), LocalTime.of(0, 0),
                LocalTime.of(8, 0), LocalTime.of(16, 0), LocalTime.of(0, 0));

        assertThat(saved.getDoses().stream().map(Dose::getStatus))
                .containsOnly(DoseStatus.PENDING);
        assertThat(saved.getDoses().stream().map(Dose::getTreatment))
                .containsOnly(saved);
    }

    @Test
    void createTreatmentGeneratesOneDailyDoseWhenIntervalIsTwentyFourHours() {
        when(medicamentRepo.findByIdAndUser(5L, currentUser)).thenReturn(Optional.of(ownedMedicament()));
        when(treatmentRepo.save(org.mockito.ArgumentMatchers.any(Treatment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        treatmentService.createTreatment(request(24));

        ArgumentCaptor<Treatment> captor = ArgumentCaptor.forClass(Treatment.class);
        verify(treatmentRepo).save(captor.capture());
        assertThat(captor.getValue().getDoses()).hasSize(2);
    }

    @Test
    void createTreatmentThrowsWhenMedicationNotOwned() {
        when(medicamentRepo.findByIdAndUser(5L, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> treatmentService.createTreatment(request(8)))
                .isInstanceOf(MedicationNotFoundException.class);
    }

    @Test
    void createTreatmentThrowsWhenEndDateBeforeStartDate() {
        CreateTreatmentRequest invalid = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 1),
                8,
                1,
                LocalTime.of(8, 0));

        assertThatThrownBy(() -> treatmentService.createTreatment(invalid))
                .isInstanceOf(InvalidTreatmentException.class);
    }

    @Test
    void markDoseMarksOwnedDoseAsTaken() {
        User owner = currentUser;
        Treatment treatment = new Treatment();
        treatment.setUser(owner);
        Dose dose = new Dose();
        dose.setId(7L);
        dose.setTreatment(treatment);
        dose.setStatus(DoseStatus.PENDING);
        when(doseRepo.findById(7L)).thenReturn(Optional.of(dose));
        when(doseRepo.save(dose)).thenReturn(dose);

        Dose result = treatmentService.markDose(7L, DoseStatus.TAKEN);

        assertThat(result.getStatus()).isEqualTo(DoseStatus.TAKEN);
        verify(doseRepo).save(dose);
    }

    @Test
    void markDoseMarksOwnedDoseAsSkipped() {
        Treatment treatment = new Treatment();
        treatment.setUser(currentUser);
        Dose dose = new Dose();
        dose.setId(7L);
        dose.setTreatment(treatment);
        dose.setStatus(DoseStatus.PENDING);
        when(doseRepo.findById(7L)).thenReturn(Optional.of(dose));
        when(doseRepo.save(dose)).thenReturn(dose);

        Dose result = treatmentService.markDose(7L, DoseStatus.SKIPPED);

        assertThat(result.getStatus()).isEqualTo(DoseStatus.SKIPPED);
    }

    @Test
    void markDoseThrowsWhenDoseNotFound() {
        when(doseRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> treatmentService.markDose(99L, DoseStatus.TAKEN))
                .isInstanceOf(DoseNotFoundException.class);
    }

    @Test
    void markDoseThrowsWhenDoseBelongsToAnotherUser() {
        User other = new User();
        other.setId(2L);
        Treatment treatment = new Treatment();
        treatment.setUser(other);
        Dose dose = new Dose();
        dose.setId(7L);
        dose.setTreatment(treatment);
        when(doseRepo.findById(7L)).thenReturn(Optional.of(dose));

        assertThatThrownBy(() -> treatmentService.markDose(7L, DoseStatus.TAKEN))
                .isInstanceOf(DoseNotFoundException.class);
    }
}
