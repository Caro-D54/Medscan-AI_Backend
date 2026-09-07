package com.medscan.app_med.service;

public class DoseNotFoundException extends RuntimeException {
    public DoseNotFoundException() {
        super("Dose not found");
    }
}
