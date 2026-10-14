package com.medscan.app_med.service;

public record AdminMetricsDto(
        long totalUsers,
        long regularUsers,
        long adminUsers,
        long newUsersLast7Days,
        long newUsersLast30Days,
        long totalMedications,
        long totalTreatments,
        long totalDoses,
        long uptimeSeconds
) {}
