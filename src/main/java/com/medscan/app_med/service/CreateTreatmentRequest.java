package com.medscan.app_med.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateTreatmentRequest(
        @NotNull Long medicationId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Positive int intervalHours,
        @Positive int doseQuantity,
        @NotNull LocalTime startTime) {
}
