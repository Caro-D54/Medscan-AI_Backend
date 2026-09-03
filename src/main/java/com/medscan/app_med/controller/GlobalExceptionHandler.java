package com.medscan.app_med.controller;

import com.medscan.app_med.service.DoseNotFoundException;
import com.medscan.app_med.service.DuplicateEmailException;
import com.medscan.app_med.service.InvalidCredentialsException;
import com.medscan.app_med.service.InvalidTreatmentException;
import com.medscan.app_med.service.MedicationNotFoundException;
import com.medscan.app_med.service.ScanProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Void> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Void> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ResponseEntity<Void> handleMedicationNotFound(MedicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(ScanProcessingException.class)
    public ResponseEntity<Void> handleScanProcessing(ScanProcessingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(InvalidTreatmentException.class)
    public ResponseEntity<Void> handleInvalidTreatment(InvalidTreatmentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(DoseNotFoundException.class)
    public ResponseEntity<Void> handleDoseNotFound(DoseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
