package com.medscan.app_med.controller;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import com.medscan.app_med.service.MedicationNotFoundException;
import com.medscan.app_med.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicamentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MedicamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("caro@medscan.com");
        currentUser.setPassword("hashed");
        currentUser.setName("Carolina");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMedicationsReturnsPage() throws Exception {
        Medicament med = new Medicament();
        med.setId(1L);
        med.setName("Acetaminophen");
        Page<Medicament> page = new PageImpl<>(List.of(med));
        when(medicationService.getMedications(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Acetaminophen"));
    }

    @Test
    void getMedicationsWithNameFilterAndPagingParams() throws Exception {
        Page<Medicament> page = new PageImpl<>(List.of());
        when(medicationService.getMedications(eq("amo"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/medications")
                        .param("name", "amo")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(medicationService).getMedications(eq("amo"), any(Pageable.class));
    }

    @Test
    void createMedicationReturns201() throws Exception {
        Medicament saved = new Medicament();
        saved.setId(1L);
        saved.setName("Acetaminophen");
        when(medicationService.createMedication(any(Medicament.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acetaminophen\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateMedicationReturns200() throws Exception {
        long id = 1L;
        Medicament updated = new Medicament();
        updated.setId(id);
        updated.setName("Paracetamol");
        when(medicationService.updateMedication(eq(id), any(Medicament.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/medications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Paracetamol\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Paracetamol"));
    }

    @Test
    void updateMedicationReturns404WhenMissing() throws Exception {
        long id = 99L;
        when(medicationService.updateMedication(eq(id), any(Medicament.class)))
                .thenThrow(new MedicationNotFoundException());

        mockMvc.perform(put("/api/v1/medications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Paracetamol\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMedicationReturns204() throws Exception {
        long id = 1L;

        mockMvc.perform(delete("/api/v1/medications/{id}", id))
                .andExpect(status().isNoContent());

        verify(medicationService).deleteMedication(id);
    }

    @Test
    void deleteMedicationReturns404WhenMissing() throws Exception {
        long id = 99L;
        doThrow(new MedicationNotFoundException()).when(medicationService).deleteMedication(id);

        mockMvc.perform(delete("/api/v1/medications/{id}", id))
                .andExpect(status().isNotFound());
    }
}
