package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;

import java.time.LocalDateTime;

public record DoseResponse(
        Long id,
        Long treatmentId,
        LocalDateTime scheduledAt,
        DoseStatus status,
        LocalDateTime notifiedAt
) {
    public static DoseResponse fromEntity(Dose dose) {
        if (dose == null) {
            return null;
        }
        Long treatmentId = dose.getTreatment() != null ? dose.getTreatment().getId() : null;
        return new DoseResponse(
                dose.getId(),
                treatmentId,
                dose.getScheduledAt(),
                dose.getStatus(),
                dose.getNotifiedAt()
        );
    }
}
