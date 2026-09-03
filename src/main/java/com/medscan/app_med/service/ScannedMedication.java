package com.medscan.app_med.service;

public record ScannedMedication(String brandName,
                                String activeIngredient,
                                String dosage,
                                String frequency) {
}
