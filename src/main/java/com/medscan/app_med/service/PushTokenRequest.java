package com.medscan.app_med.service;

import jakarta.validation.constraints.NotBlank;

public record PushTokenRequest(@NotBlank String pushToken) {
}
