package com.medscan.app_med.service;

public class ScanProcessingException extends RuntimeException {

    public ScanProcessingException(String message) {
        super(message);
    }

    public ScanProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
