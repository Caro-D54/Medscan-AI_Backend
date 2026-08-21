package com.medscan.app_med.controller;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.repository.MedicamentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicaments")
public class MedicamentController {
    @Autowired 
    private MedicamentRepo medicamentRepo;

    @GetMapping
    public List<Medicament> getAllMedicaments() {
        return medicamentRepo.findAll();
    }
        
    @PostMapping public Medicament guardar(@RequestBody Medicament medicament) {
        return medicamentRepo.save(medicament);
    }
    
}
