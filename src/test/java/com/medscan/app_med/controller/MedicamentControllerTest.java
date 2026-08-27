package com.medscan.app_med.controller;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.repository.MedicamentRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicamentControllerTest {

    @Mock
    private MedicamentRepo medicamentRepo;

    @InjectMocks
    private MedicamentController controller;

    @Test
    void getAllMedicamentsReturnsRepositoryResults() {
        List<Medicament> medicaments = List.of(new Medicament());
        when(medicamentRepo.findAll()).thenReturn(medicaments);

        List<Medicament> result = controller.getAllMedicaments();

        assertThat(result).isSameAs(medicaments);
        verify(medicamentRepo).findAll();
    }

    @Test
    void guardarSavesAndReturnsTheMedicament() {
        Medicament medicament = new Medicament();
        when(medicamentRepo.save(medicament)).thenReturn(medicament);

        Medicament result = controller.guardar(medicament);

        assertThat(result).isSameAs(medicament);
        verify(medicamentRepo).save(medicament);
    }
}
