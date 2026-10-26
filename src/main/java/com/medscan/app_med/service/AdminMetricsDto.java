package com.medscan.app_med.service;

public record AdminMetricsDto(
        long totalUsers,
        long regularUsers,
        long adminUsers,
        long loggedInUsers,
        long newUsersLast7Days,
        long newUsersLast30Days,
        long totalMedications,
        long totalTreatments,
        long totalDoses,
        long uptimeSeconds
) {
    public AdminMetricsDto(
            long totalUsers,
            long regularUsers,
            long adminUsers,
            long newUsersLast7Days,
            long newUsersLast30Days,
            long totalMedications,
            long totalTreatments,
            long totalDoses,
            long uptimeSeconds
    ) {
        this(
                totalUsers,
                regularUsers,
                adminUsers,
                0L,
                newUsersLast7Days,
                newUsersLast30Days,
                totalMedications,
                totalTreatments,
                totalDoses,
                uptimeSeconds
        );
    }
}
