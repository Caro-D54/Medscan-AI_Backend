package com.medscan.app_med.service;

import com.medscan.app_med.model.Treatment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TreatmentResponse(
        Long id,
        Long medicationId,
        String medicationName,
        LocalDate startDate,
        LocalDate endDate,
        int intervalHours,
        int doseQuantity,
        LocalTime startTime,
        List<DoseResponse> doses
) {
    public static TreatmentResponse fromEntity(Treatment treatment) {
        if (treatment == null) {
            return null;
        }
        Long medicationId = treatment.getMedicament() != null ? treatment.getMedicament().getId() : null;
        String medicationName = treatment.getMedicament() != null ? treatment.getMedicament().getName() : null;

        List<DoseResponse> doseDtos = treatment.getDoses() == null
                ? List.of()
                : treatment.getDoses().stream().map(DoseResponse::fromEntity).toList();

        return new TreatmentResponse(
                treatment.getId(),
                medicationId,
                medicationName,
                treatment.getStartDate(),
                treatment.getEndDate(),
                treatment.getIntervalHours(),
                treatment.getDoseQuantity(),
                treatment.getStartTime(),
                doseDtos
        );
    }
}
