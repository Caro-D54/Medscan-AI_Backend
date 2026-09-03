package com.medscan.app_med.service;

import com.medscan.app_med.model.DoseStatus;
import jakarta.validation.constraints.NotNull;

public record TakeDoseRequest(@NotNull DoseStatus status) {
}
