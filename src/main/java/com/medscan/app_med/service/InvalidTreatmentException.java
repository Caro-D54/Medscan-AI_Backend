package com.medscan.app_med.service;

public class InvalidTreatmentException extends RuntimeException {
    public InvalidTreatmentException(String message) {
        super(message);
    }
}
