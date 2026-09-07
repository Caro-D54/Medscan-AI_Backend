package com.medscan.app_med.controller;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.service.CreateTreatmentRequest;
import com.medscan.app_med.service.TakeDoseRequest;
import com.medscan.app_med.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping
    public ResponseEntity<Treatment> create(@Valid @RequestBody CreateTreatmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treatmentService.createTreatment(request));
    }

    @PatchMapping("/doses/{id}/take")
    public ResponseEntity<Dose> markDose(@PathVariable Long id,
                                         @Valid @RequestBody TakeDoseRequest request) {
        return ResponseEntity.ok(treatmentService.markDose(id, request.status()));
    }
}
