package com.medscan.app_med.controller;

import com.medscan.app_med.service.DoseNotFoundException;
import com.medscan.app_med.service.DuplicateEmailException;
import com.medscan.app_med.service.InvalidCredentialsException;
import com.medscan.app_med.service.InvalidTreatmentException;
import com.medscan.app_med.service.MedicationNotFoundException;
import com.medscan.app_med.service.ScanProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "El email ya está registrado"
        );
        problem.setTitle("Email duplicado");
        problem.setType(URI.create("https://medscan.com/errors/duplicate-email"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas"
        );
        problem.setTitle("No autorizado");
        problem.setType(URI.create("https://medscan.com/errors/invalid-credentials"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Uno o más campos contienen valores inválidos"
        );
        problem.setTitle("Error de validación");
        problem.setType(URI.create("https://medscan.com/errors/validation-error"));
        problem.setProperty("timestamp", Instant.now());
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ProblemDetail handleMedicationNotFound(MedicationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Medicamento no encontrado"
        );
        problem.setTitle("Recurso no encontrado");
        problem.setType(URI.create("https://medscan.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ScanProcessingException.class)
    public ProblemDetail handleScanProcessing(ScanProcessingException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Error al procesar escaneo"
        );
        problem.setTitle("Error de procesamiento");
        problem.setType(URI.create("https://medscan.com/errors/scan-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidTreatmentException.class)
    public ProblemDetail handleInvalidTreatment(InvalidTreatmentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Datos de tratamiento inválidos"
        );
        problem.setTitle("Tratamiento inválido");
        problem.setType(URI.create("https://medscan.com/errors/invalid-treatment"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(DoseNotFoundException.class)
    public ProblemDetail handleDoseNotFound(DoseNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Dosis no encontrada"
        );
        problem.setTitle("Recurso no encontrado");
        problem.setType(URI.create("https://medscan.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
