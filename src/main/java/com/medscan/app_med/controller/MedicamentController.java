package com.medscan.app_med.controller;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicamentController {

    private final MedicationService medicationService;

    public MedicamentController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public Page<Medicament> getMedications(@RequestParam(required = false) String name,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return medicationService.getMedications(name, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
    }

    @PostMapping
    public ResponseEntity<Medicament> createMedication(@Valid @RequestBody Medicament medicament) {
        Medicament saved = medicationService.createMedication(medicament);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicament> updateMedication(@PathVariable Long id,
                                                       @Valid @RequestBody Medicament medicament) {
        return ResponseEntity.ok(medicationService.updateMedication(id, medicament));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }
}
