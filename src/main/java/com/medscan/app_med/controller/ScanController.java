package com.medscan.app_med.controller;

import com.medscan.app_med.service.ScannedMedication;
import com.medscan.app_med.service.ScanProcessingException;
import com.medscan.app_med.service.ScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/scan")
public class ScanController {

    private final ScannerService scannerService;

    public ScanController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/process")
    public ResponseEntity<ScannedMedication> process(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ScannedMedication medication = scannerService.scan(getBytes(file), file.getContentType());
        return ResponseEntity.ok(medication);
    }

    private byte[] getBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new ScanProcessingException("No se pudo leer la imagen recibida", e);
        }
    }
}
