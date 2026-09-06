package com.medscan.app_med.service;

import com.medscan.app_med.dto.MedicamentRequest;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.repository.MedicamentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MedicamentService {

    private final MedicamentRepo medicamentRepo;

    public MedicamentService(MedicamentRepo medicamentRepo) {
        this.medicamentRepo = medicamentRepo;
    }

    @Transactional(readOnly = true)
    public Page<Medicament> findAll(Pageable pageable) {
        return medicamentRepo.findAll(pageable);
    }

    @Transactional
    public Medicament create(MedicamentRequest request) {
        if (medicamentRepo.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un medicamento con el nombre '" + request.name() + "'");
        }
        return medicamentRepo.save(request.toEntity());
    }
}