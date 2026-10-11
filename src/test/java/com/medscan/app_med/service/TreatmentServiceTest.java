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

        List<java.time.LocalDateTime> dateTimes = saved.getDoses().stream()
                .map(Dose::getScheduledAt)
                .toList();
        assertThat(dateTimes).containsExactly(
                java.time.LocalDateTime.of(2026, 9, 1, 8, 0),
                java.time.LocalDateTime.of(2026, 9, 1, 16, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 0, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 8, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 16, 0),
                java.time.LocalDateTime.of(2026, 9, 3, 0, 0));
        assertThat(dateTimes).isSorted();

        assertThat(saved.getDoses().stream().map(Dose::getStatus))
                .containsOnly(DoseStatus.PENDING);
        assertThat(saved.getDoses().stream().map(Dose::getTreatment))
                .containsOnly(saved);
    }

    @Test
    void createTreatmentSchedulesInStrictChronologicalOrderWhenStartTimeCrossesMidnight() {
        when(medicamentRepo.findByIdAndUser(5L, currentUser)).thenReturn(Optional.of(ownedMedicament()));
        when(treatmentRepo.save(org.mockito.ArgumentMatchers.any(Treatment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateTreatmentRequest nightRequest = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                8,
                1,
                LocalTime.of(20, 0));

        Treatment saved = treatmentService.createTreatment(nightRequest);

        List<java.time.LocalDateTime> scheduledTimes = saved.getDoses().stream()
                .map(Dose::getScheduledAt)
                .toList();

        assertThat(scheduledTimes).containsExactly(
                java.time.LocalDateTime.of(2026, 9, 1, 20, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 4, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 12, 0),
                java.time.LocalDateTime.of(2026, 9, 2, 20, 0),
                java.time.LocalDateTime.of(2026, 9, 3, 4, 0),
                java.time.LocalDateTime.of(2026, 9, 3, 12, 0));
        assertThat(scheduledTimes).isSorted();
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
                .isInstanceOf(InvalidTreatmentException.class)
                .hasMessageContaining("anterior a la de inicio");
    }

    @Test
    void createTreatmentThrowsWhenDurationExceeds365Days() {
        CreateTreatmentRequest tooLong = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 5),
                8,
                1,
                LocalTime.of(8, 0));

        assertThatThrownBy(() -> treatmentService.createTreatment(tooLong))
                .isInstanceOf(InvalidTreatmentException.class)
                .hasMessageContaining("365 días");
    }

    @Test
    void createTreatmentThrowsWhenIntervalHoursIsZeroOrNegative() {
        CreateTreatmentRequest zeroInterval = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                0,
                1,
                LocalTime.of(8, 0));

        assertThatThrownBy(() -> treatmentService.createTreatment(zeroInterval))
                .isInstanceOf(InvalidTreatmentException.class)
                .hasMessageContaining("al menos 1 hora");
    }

    @Test
    void createTreatmentThrowsWhenIntervalHoursExceedsMax() {
        CreateTreatmentRequest hugeInterval = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                200,
                1,
                LocalTime.of(8, 0));

        assertThatThrownBy(() -> treatmentService.createTreatment(hugeInterval))
                .isInstanceOf(InvalidTreatmentException.class)
                .hasMessageContaining("168 horas");
    }

    @Test
    void createTreatmentThrowsWhenDoseQuantityIsZeroOrNegative() {
        CreateTreatmentRequest invalidQuantity = new CreateTreatmentRequest(
                5L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                8,
                0,
                LocalTime.of(8, 0));

        assertThatThrownBy(() -> treatmentService.createTreatment(invalidQuantity))
                .isInstanceOf(InvalidTreatmentException.class)
                .hasMessageContaining("cantidad por dosis");
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

    @Test
    void getActiveTreatmentsReturnsActiveTreatmentsForCurrentUser() {
        Treatment treatment = new Treatment();
        treatment.setId(10L);
        treatment.setUser(currentUser);
        when(treatmentRepo.findActiveByUser(org.mockito.ArgumentMatchers.eq(currentUser), org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of(treatment));

        List<Treatment> results = treatmentService.getActiveTreatments();

        assertThat(results).containsExactly(treatment);
        verify(treatmentRepo).findActiveByUser(org.mockito.ArgumentMatchers.eq(currentUser), org.mockito.ArgumentMatchers.any(LocalDate.class));
    }

    @Test
    void getTreatmentsWithActiveOnlyFalseReturnsAllTreatments() {
        Treatment treatment = new Treatment();
        treatment.setId(11L);
        treatment.setUser(currentUser);
        when(treatmentRepo.findAllByUser(currentUser)).thenReturn(List.of(treatment));

        List<Treatment> results = treatmentService.getTreatments(false);

        assertThat(results).containsExactly(treatment);
        verify(treatmentRepo).findAllByUser(currentUser);
    }

    @Test
    void getTreatmentsThrowsWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> treatmentService.getActiveTreatments())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user");
    }
}
