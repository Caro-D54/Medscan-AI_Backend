package com.medscan.app_med.dto;

import com.medscan.app_med.model.Medicament;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicamentRequest(
        @NotBlank(message = "El nombre del medicamento es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @Size(max = 255, message = "El componente activo no puede superar los 255 caracteres")
        String componentActive,

        @Size(max = 1000, message = "Los efectos secundarios no pueden superar los 1000 caracteres")
        String secondaryEffect,

        @Size(max = 1000, message = "Las interacciones peligrosas no pueden superar los 1000 caracteres")
        String dangerousInteractions,

        boolean withFood
) {
    public Medicament toEntity() {
        Medicament medicament = new Medicament();
        medicament.setName(name);
        medicament.setComponentActive(componentActive);
        medicament.setSecondaryEffect(secondaryEffect);
        medicament.setDangerousInteractions(dangerousInteractions);
        medicament.setWithFood(withFood);
        return medicament;
    }
}