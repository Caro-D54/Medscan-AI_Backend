package com.medscan.app_med.controller;

import com.medscan.app_med.service.CreateTreatmentRequest;
import com.medscan.app_med.service.DoseResponse;
import com.medscan.app_med.service.TakeDoseRequest;
import com.medscan.app_med.service.TreatmentResponse;
import com.medscan.app_med.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody CreateTreatmentRequest request) {
        TreatmentResponse response = TreatmentResponse.fromEntity(treatmentService.createTreatment(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TreatmentResponse>> getTreatments(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "true") boolean activeOnly) {
        List<TreatmentResponse> responses = treatmentService.getTreatments(activeOnly)
                .stream()
                .map(TreatmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/doses/{id}/take")
    public ResponseEntity<DoseResponse> markDose(@PathVariable Long id,
                                                 @Valid @RequestBody TakeDoseRequest request) {
        DoseResponse response = DoseResponse.fromEntity(treatmentService.markDose(id, request.status()));
        return ResponseEntity.ok(response);
    }
}
