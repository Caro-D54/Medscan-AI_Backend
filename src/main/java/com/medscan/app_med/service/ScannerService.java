package com.medscan.app_med.service;

public interface ScannerService {

    ScannedMedication scan(byte[] imageBytes, String contentType);
}
