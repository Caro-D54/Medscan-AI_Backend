package com.medscan.app_med.service;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.MedicamentRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicamentRepo medicamentRepo;

    @InjectMocks
    private MedicationService medicationService;

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

    @Test
    void getMedicationsWithoutFilterReturnsAllForCurrentUser() {
        Page<Medicament> meds = new PageImpl<>(List.of(new Medicament()));
        Pageable pageable = PageRequest.of(0, 20);
        when(medicamentRepo.findByUser(currentUser, pageable)).thenReturn(meds);

        Page<Medicament> result = medicationService.getMedications(null, pageable);

        assertThat(result).isSameAs(meds);
        verify(medicamentRepo).findByUser(currentUser, pageable);
    }

    @Test
    void getMedicationsWithNameFilterUsesContainsQuery() {
        Page<Medicament> meds = new PageImpl<>(List.of(new Medicament()));
        Pageable pageable = PageRequest.of(1, 10);
        when(medicamentRepo.findByUserAndNameContainingIgnoreCase(currentUser, "amo", pageable))
                .thenReturn(meds);

        Page<Medicament> result = medicationService.getMedications("amo", pageable);

        assertThat(result).isSameAs(meds);
        verify(medicamentRepo).findByUserAndNameContainingIgnoreCase(currentUser, "amo", pageable);
    }

    @Test
    void createMedicationSetsCurrentUserAndSaves() {
        Medicament medicament = new Medicament();
        medicament.setName("Acetaminophen");
        Medicament saved = new Medicament();
        saved.setId(5L);
        when(medicamentRepo.save(medicament)).thenReturn(saved);

        Medicament result = medicationService.createMedication(medicament);

        assertThat(result).isSameAs(saved);
        assertThat(medicament.getUser()).isSameAs(currentUser);
        verify(medicamentRepo).save(medicament);
    }

    @Test
    void updateMedicationUpdatesExistingOwnedMedication() {
        Long id = 5L;
        Medicament existing = new Medicament();
        existing.setId(id);
        existing.setName("Old");
        existing.setUser(currentUser);

        Medicament patch = new Medicament();
        patch.setName("New");
        patch.setComponentActive("Paracetamol");

        when(medicamentRepo.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(existing));
        when(medicamentRepo.save(existing)).thenReturn(existing);

        Medicament result = medicationService.updateMedication(id, patch);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getComponentActive()).isEqualTo("Paracetamol");
        verify(medicamentRepo).save(existing);
    }

    @Test
    void updateMedicationThrowsWhenNotFound() {
        Long id = 99L;
        when(medicamentRepo.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.updateMedication(id, new Medicament()))
                .isInstanceOf(MedicationNotFoundException.class);
    }

    @Test
    void deleteMedicationDeletesOwnedMedication() {
        Long id = 5L;
        Medicament existing = new Medicament();
        existing.setId(id);
        when(medicamentRepo.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(existing));

        medicationService.deleteMedication(id);

        verify(medicamentRepo).delete(existing);
    }

    @Test
    void deleteMedicationThrowsWhenNotFound() {
        Long id = 99L;
        when(medicamentRepo.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.deleteMedication(id))
                .isInstanceOf(MedicationNotFoundException.class);
    }
}
