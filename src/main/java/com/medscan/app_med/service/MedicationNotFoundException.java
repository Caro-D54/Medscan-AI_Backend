package com.medscan.app_med.service;

public class MedicationNotFoundException extends RuntimeException {
    public MedicationNotFoundException() {
        super("Medication not found");
    }
}
