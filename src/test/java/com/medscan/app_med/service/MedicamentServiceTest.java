package com.medscan.app_med.service;

import com.medscan.app_med.dto.MedicamentRequest;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.repository.MedicamentRepo;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MedicamentServiceTest {

    private final MedicamentRepo repo = mock(MedicamentRepo.class);
    private final MedicamentService service = new MedicamentService(repo);

    @Test
    void create_validRequest_savesAndReturnsMedicament() {
        MedicamentRequest request = new MedicamentRequest("Aspirina", "Acido acetilsalicilico", null, null, true);
        Medicament saved = new Medicament();
        saved.setId(1L);
        saved.setName("Aspirina");

        when(repo.existsByNameIgnoreCase("Aspirina")).thenReturn(false);
        when(repo.save(any(Medicament.class))).thenReturn(saved);

        Medicament result = service.create(request);

        assertThat(result).isSameAs(saved);
        verify(repo).save(any(Medicament.class));
    }

    @Test
    void create_duplicateName_throwsConflict() {
        MedicamentRequest request = new MedicamentRequest("aspirina", null, null, null, false);

        when(repo.existsByNameIgnoreCase("aspirina")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe");
        verify(repo, never()).save(any());
    }
}