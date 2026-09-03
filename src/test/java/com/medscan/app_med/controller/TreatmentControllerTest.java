package com.medscan.app_med.controller;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import com.medscan.app_med.service.CreateTreatmentRequest;
import com.medscan.app_med.service.DoseNotFoundException;
import com.medscan.app_med.service.MedicationNotFoundException;
import com.medscan.app_med.service.TakeDoseRequest;
import com.medscan.app_med.service.TreatmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreatmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TreatmentService treatmentService;

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

    private Treatment treatmentWithScheduledDoses() {
        Medicament medicament = new Medicament();
        medicament.setId(5L);
        medicament.setName("Acetaminophen");

        Treatment treatment = new Treatment();
        treatment.setId(1L);
        treatment.setMedicament(medicament);
        treatment.setDoseQuantity(1);
        treatment.setIntervalHours(8);

        Dose dose = new Dose();
        dose.setId(7L);
        dose.setTreatment(treatment);
        dose.setScheduledAt(LocalDateTime.of(2026, 9, 1, 8, 0));
        dose.setStatus(DoseStatus.PENDING);
        treatment.getDoses().add(dose);
        return treatment;
    }

    @Test
    void createTreatmentReturns201WithGeneratedSchedule() throws Exception {
        when(treatmentService.createTreatment(any(CreateTreatmentRequest.class)))
                .thenReturn(treatmentWithScheduledDoses());

        mockMvc.perform(post("/api/v1/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medicationId": 5,
                                  "startDate": "2026-09-01",
                                  "endDate": "2026-09-02",
                                  "intervalHours": 8,
                                  "doseQuantity": 1,
                                  "startTime": "08:00:00"
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.medicationId").value(5))
                .andExpect(jsonPath("$.doses.length()").value(1))
                .andExpect(jsonPath("$.doses[0].status").value("PENDING"));
    }

    @Test
    void createTreatmentReturns404WhenMedicationNotOwned() throws Exception {
        when(treatmentService.createTreatment(any(CreateTreatmentRequest.class)))
                .thenThrow(new MedicationNotFoundException());

        mockMvc.perform(post("/api/v1/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medicationId": 99,
                                  "startDate": "2026-09-01",
                                  "endDate": "2026-09-02",
                                  "intervalHours": 8,
                                  "doseQuantity": 1,
                                  "startTime": "08:00:00"
                                }"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void markDoseReturnsUpdatedDose() throws Exception {
        Dose dose = new Dose();
        dose.setId(7L);
        dose.setStatus(DoseStatus.TAKEN);
        when(treatmentService.markDose(eq(7L), eq(DoseStatus.TAKEN))).thenReturn(dose);

        mockMvc.perform(patch("/api/v1/treatments/doses/7/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "TAKEN" }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("TAKEN"));
    }

    @Test
    void markDoseReturns404WhenDoseNotFound() throws Exception {
        when(treatmentService.markDose(eq(99L), eq(DoseStatus.TAKEN)))
                .thenThrow(new DoseNotFoundException());

        mockMvc.perform(patch("/api/v1/treatments/doses/99/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "TAKEN" }"""))
                .andExpect(status().isNotFound());
    }
}
