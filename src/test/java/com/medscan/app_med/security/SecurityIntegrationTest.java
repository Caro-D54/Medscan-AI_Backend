package com.medscan.app_med.security;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.MedicamentRepo;
import com.medscan.app_med.repository.TreatmentRepo;
import com.medscan.app_med.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MedicamentRepo medicamentRepo;

    @Autowired
    private TreatmentRepo treatmentRepo;

    @Test
    void registerEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"nuevo@medscan.com","password":"secret123","name":"Nuevo"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void protectedRouteWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/medications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteWithInvalidTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/medications")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedRouteWithValidTokenReturns200() throws Exception {
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");
        user = userRepo.save(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/medications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getMeWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMeWithValidTokenReturns200AndCurrentUserDetails() throws Exception {
        User user = new User();
        user.setEmail("getme@medscan.com");
        user.setPassword("hashed");
        user.setName("GetMe User");
        User savedUser = userRepo.save(user);

        String token = jwtService.generateToken(savedUser);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("GetMe User"))
                .andExpect(jsonPath("$.email").value("getme@medscan.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerAndLoginReturnTokenAndUserPayload() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"fullauth@medscan.com","password":"secretPass123","name":"Full Auth"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Full Auth"))
                .andExpect(jsonPath("$.user.email").value("fullauth@medscan.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"fullauth@medscan.com","password":"secretPass123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Full Auth"))
                .andExpect(jsonPath("$.user.email").value("fullauth@medscan.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void registerAdminReturnsRoleAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"adminuser@medscan.com","password":"secretPass123","name":"Admin User","role":"ADMIN"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void getMedicationByIdWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/medications/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMedicationByIdWithValidTokenReturns200AndMedicationDetails() throws Exception {
        User user = new User();
        user.setEmail("medowner@medscan.com");
        user.setPassword("hashed");
        user.setName("Med Owner");
        User savedUser = userRepo.save(user);

        Medicament med = new Medicament();
        med.setName("Amoxicilina 500mg");
        med.setComponentActive("Amoxicilina");
        med.setSecondaryEffect("Náuseas leves");
        med.setWithFood(true);
        med.setUser(savedUser);
        Medicament savedMed = medicamentRepo.save(med);

        String token = jwtService.generateToken(savedUser);

        mockMvc.perform(get("/api/v1/medications/" + savedMed.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMed.getId()))
                .andExpect(jsonPath("$.name").value("Amoxicilina 500mg"))
                .andExpect(jsonPath("$.componentActive").value("Amoxicilina"))
                .andExpect(jsonPath("$.secondaryEffect").value("Náuseas leves"))
                .andExpect(jsonPath("$.withFood").value(true));
    }

    @Test
    void getMedicationByIdBelongingToAnotherUserReturns404() throws Exception {
        User owner = new User();
        owner.setEmail("owner123@medscan.com");
        owner.setPassword("hashed");
        owner.setName("Owner");
        owner = userRepo.save(owner);

        User other = new User();
        other.setEmail("other123@medscan.com");
        other.setPassword("hashed");
        other.setName("Other");
        other = userRepo.save(other);

        Medicament med = new Medicament();
        med.setName("Ibuprofeno 600mg");
        med.setUser(owner);
        med = medicamentRepo.save(med);

        String otherToken = jwtService.generateToken(other);

        mockMvc.perform(get("/api/v1/medications/" + med.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMedicationWithPosologyPayloadEndToEnd() throws Exception {
        User user = new User();
        user.setEmail("creator@medscan.com");
        user.setPassword("hashed");
        user.setName("Creator");
        user = userRepo.save(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/api/v1/medications")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Paracetamol 500mg",
                                  "componentActive": "Paracetamol",
                                  "dosage": "500mg",
                                  "frequency": "cada 8 horas",
                                  "instructions": "Tomar con agua",
                                  "withFood": false
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$.componentActive").value("Paracetamol"));
    }

    @Test
    void getTreatmentsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/treatments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTreatmentsWithValidTokenReturns200AndActiveTreatmentsWithDoses() throws Exception {
        LocalDate today = LocalDate.now();

        User user = new User();
        user.setEmail("treatmentowner@medscan.com");
        user.setPassword("hashed");
        user.setName("Treatment Owner");
        user = userRepo.save(user);

        Medicament med = new Medicament();
        med.setName("Amoxicilina 875mg");
        med.setUser(user);
        med = medicamentRepo.save(med);

        Treatment treatment = new Treatment();
        treatment.setUser(user);
        treatment.setMedicament(med);
        treatment.setStartDate(today);
        treatment.setEndDate(today.plusDays(5));
        treatment.setIntervalHours(12);
        treatment.setDoseQuantity(1);
        treatment.setStartTime(LocalTime.of(9, 0));

        Dose dose = new Dose();
        dose.setTreatment(treatment);
        dose.setScheduledAt(LocalDateTime.of(today, LocalTime.of(9, 0)));
        dose.setStatus(DoseStatus.PENDING);
        treatment.getDoses().add(dose);

        Treatment savedTreatment = treatmentRepo.save(treatment);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/treatments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(savedTreatment.getId()))
                .andExpect(jsonPath("$[0].medicationName").value("Amoxicilina 875mg"))
                .andExpect(jsonPath("$[0].intervalHours").value(12))
                .andExpect(jsonPath("$[0].doses.length()").value(1))
                .andExpect(jsonPath("$[0].doses[0].status").value("PENDING"));
    }

    @Test
    void getTreatmentsExcludesOtherUsersTreatments() throws Exception {
        LocalDate today = LocalDate.now();

        User userA = new User();
        userA.setEmail("treatA@medscan.com");
        userA.setPassword("hashed");
        userA.setName("User A");
        userA = userRepo.save(userA);

        User userB = new User();
        userB.setEmail("treatB@medscan.com");
        userB.setPassword("hashed");
        userB.setName("User B");
        userB = userRepo.save(userB);

        Medicament med = new Medicament();
        med.setName("Aspirina");
        med.setUser(userA);
        med = medicamentRepo.save(med);

        Treatment treatment = new Treatment();
        treatment.setUser(userA);
        treatment.setMedicament(med);
        treatment.setStartDate(today);
        treatment.setEndDate(today.plusDays(3));
        treatment.setIntervalHours(8);
        treatment.setDoseQuantity(1);
        treatment.setStartTime(LocalTime.of(8, 0));
        treatmentRepo.save(treatment);

        String tokenB = jwtService.generateToken(userB);

        mockMvc.perform(get("/api/v1/treatments")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
