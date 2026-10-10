package com.medscan.app_med.controller;

import com.medscan.app_med.service.DoseNotFoundException;
import com.medscan.app_med.service.DuplicateEmailException;
import com.medscan.app_med.service.InvalidCredentialsException;
import com.medscan.app_med.service.InvalidTreatmentException;
import com.medscan.app_med.service.MedicationNotFoundException;
import com.medscan.app_med.service.ScanProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDuplicateEmailReturnsBadRequestWithDetails() {
        ProblemDetail problem = handler.handleDuplicateEmail(new DuplicateEmailException("test@medscan.com"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Email duplicado", problem.getTitle());
        assertTrue(problem.getDetail().contains("test@medscan.com"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    @Test
    void handleInvalidCredentialsReturnsUnauthorized() {
        ProblemDetail problem = handler.handleInvalidCredentials(new InvalidCredentialsException());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("No autorizado", problem.getTitle());
    }

    @Test
    void handleMedicationNotFoundReturnsNotFound() {
        ProblemDetail problem = handler.handleMedicationNotFound(new MedicationNotFoundException());

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso no encontrado", problem.getTitle());
    }

    @Test
    void handleDoseNotFoundReturnsNotFound() {
        ProblemDetail problem = handler.handleDoseNotFound(new DoseNotFoundException());

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso no encontrado", problem.getTitle());
    }

    @Test
    void handleScanProcessingReturnsInternalServerError() {
        ProblemDetail problem = handler.handleScanProcessing(new ScanProcessingException("Error en OCR"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Error de procesamiento", problem.getTitle());
        assertEquals("Error en OCR", problem.getDetail());
    }

    @Test
    void handleInvalidTreatmentReturnsBadRequest() {
        ProblemDetail problem = handler.handleInvalidTreatment(new InvalidTreatmentException("Fecha inválida"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Tratamiento inválido", problem.getTitle());
        assertEquals("Fecha inválida", problem.getDetail());
    }
}
