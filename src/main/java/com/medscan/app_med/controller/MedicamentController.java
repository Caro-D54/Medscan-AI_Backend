package com.medscan.app_med.controller;

import com.medscan.app_med.dto.MedicamentRequest;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.service.MedicamentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    public MedicamentController(MedicamentService medicamentService) {
        this.medicamentService = medicamentService;
    }

    @GetMapping
    public Page<Medicament> getAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return medicamentService.findAll(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medicament create(@Valid @RequestBody MedicamentRequest request) {
        return medicamentService.create(request);
    }
}