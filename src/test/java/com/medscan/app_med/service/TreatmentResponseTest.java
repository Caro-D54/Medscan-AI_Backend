package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentResponseTest {

    @Test
    void fromEntityMapsAllFieldsCorrectly() {
        Medicament med = new Medicament();
        med.setId(10L);
        med.setName("Paracetamol");

        Treatment treatment = new Treatment();
        treatment.setId(1L);
        treatment.setMedicament(med);
        treatment.setStartDate(LocalDate.of(2026, 10, 1));
        treatment.setEndDate(LocalDate.of(2026, 10, 5));
        treatment.setIntervalHours(8);
        treatment.setDoseQuantity(1);
        treatment.setStartTime(LocalTime.of(8, 0));

        Dose dose = new Dose();
        dose.setId(100L);
        dose.setTreatment(treatment);
        dose.setScheduledAt(LocalDateTime.of(2026, 10, 1, 8, 0));
        dose.setStatus(DoseStatus.PENDING);
        treatment.setDoses(List.of(dose));

        TreatmentResponse response = TreatmentResponse.fromEntity(treatment);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(10L, response.medicationId());
        assertEquals("Paracetamol", response.medicationName());
        assertEquals(LocalDate.of(2026, 10, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 10, 5), response.endDate());
        assertEquals(8, response.intervalHours());
        assertEquals(1, response.doseQuantity());
        assertEquals(LocalTime.of(8, 0), response.startTime());
        assertEquals(1, response.doses().size());

        DoseResponse doseResponse = response.doses().get(0);
        assertEquals(100L, doseResponse.id());
        assertEquals(1L, doseResponse.treatmentId());
        assertEquals(DoseStatus.PENDING, doseResponse.status());
    }

    @Test
    void fromEntityHandlesNullSafely() {
        assertNull(TreatmentResponse.fromEntity(null));
        assertNull(DoseResponse.fromEntity(null));
    }
}
