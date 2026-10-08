package com.medscan.app_med.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.medscan.app_med.model.Medicament;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedicamentRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        String componentActive,
        String secondaryEffect,
        boolean withFood,
        String dangerousInteractions,
        String dosage,
        String frequency,
        String instructions
) {
    public Medicament toEntity() {
        Medicament m = new Medicament();
        m.setName(name);
        String active = (componentActive != null && !componentActive.isBlank()) ? componentActive : dosage;
        m.setComponentActive(active);
        String secondary = (secondaryEffect != null && !secondaryEffect.isBlank()) ? secondaryEffect : instructions;
        m.setSecondaryEffect(secondary);

        m.setWithFood(withFood);
        m.setDangerousInteractions(dangerousInteractions);
        return m;
    }
}
