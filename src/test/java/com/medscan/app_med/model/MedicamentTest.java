package com.medscan.app_med.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedicamentTest {

    @Test
    void newMedicamentHasDefaultValues() {
        Medicament medicament = new Medicament();

        assertThat(medicament.getId()).isNull();
        assertThat(medicament.getName()).isNull();
        assertThat(medicament.getComponentActive()).isNull();
        assertThat(medicament.getSecondaryEffect()).isNull();
        assertThat(medicament.isWithFood()).isFalse();
        assertThat(medicament.getDangerousInteractions()).isNull();
        assertThat(medicament.getDosage()).isNull();
        assertThat(medicament.getFrequency()).isNull();
        assertThat(medicament.getInstructions()).isNull();
    }

    @Test
    void medicamentStoresAllProperties() {
        Medicament medicament = new Medicament();

        medicament.setId(7L);
        medicament.setName("Acetaminophen");
        medicament.setComponentActive("Paracetamol");
        medicament.setSecondaryEffect("Nausea");
        medicament.setWithFood(true);
        medicament.setDangerousInteractions("Alcohol");
        medicament.setDosage("500mg");
        medicament.setFrequency("cada 8 horas");
        medicament.setInstructions("Tomar con agua");

        assertThat(medicament.getId()).isEqualTo(7L);
        assertThat(medicament.getName()).isEqualTo("Acetaminophen");
        assertThat(medicament.getComponentActive()).isEqualTo("Paracetamol");
        assertThat(medicament.getSecondaryEffect()).isEqualTo("Nausea");
        assertThat(medicament.isWithFood()).isTrue();
        assertThat(medicament.getDangerousInteractions()).isEqualTo("Alcohol");
        assertThat(medicament.getDosage()).isEqualTo("500mg");
        assertThat(medicament.getFrequency()).isEqualTo("cada 8 horas");
        assertThat(medicament.getInstructions()).isEqualTo("Tomar con agua");
    }
}
