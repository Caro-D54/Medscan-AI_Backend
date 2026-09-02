package com.medscan.app_med.repository;

import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MedicamentRepoTest {

    @Autowired
    private MedicamentRepo medicamentRepo;

    @Autowired
    private UserRepo userRepo;

    private User saveUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashed");
        user.setName("Carolina");
        return userRepo.save(user);
    }

    private Medicament saveMedicament(User user, String name) {
        Medicament medicament = new Medicament();
        medicament.setName(name);
        medicament.setUser(user);
        return medicamentRepo.save(medicament);
    }

    @Test
    void findByIdAndUserReturnsMedicamentOwnedByUser() {
        User user = saveUser("caro@medscan.com");
        Medicament med = saveMedicament(user, "Acetaminophen");

        Optional<Medicament> result = medicamentRepo.findByIdAndUser(med.getId(), user);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Acetaminophen");
    }

    @Test
    void findByIdAndUserReturnsEmptyForOtherUsersMedication() {
        User owner = saveUser("owner@medscan.com");
        User other = saveUser("other@medscan.com");
        Medicament med = saveMedicament(owner, "Ibuprofen");

        Optional<Medicament> result = medicamentRepo.findByIdAndUser(med.getId(), other);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserOrderByIdAscReturnsOnlyThatUsersMedications() {
        User userA = saveUser("a@medscan.com");
        User userB = saveUser("b@medscan.com");
        saveMedicament(userA, "Aspirin");
        saveMedicament(userB, "Paracetamol");
        saveMedicament(userA, "Ibuprofen");

        List<Medicament> result = medicamentRepo.findByUserOrderByIdAsc(userA);

        assertThat(result).extracting(Medicament::getName)
                .containsExactly("Aspirin", "Ibuprofen");
    }

    @Test
    void findByUserAndNameContainingIgnoreCaseOrderByIdAscFiltersByName() {
        User user = saveUser("caro@medscan.com");
        saveMedicament(user, "Acetaminophen");
        saveMedicament(user, "Amoxicillin");
        saveMedicament(user, "Ibuprofen");

        List<Medicament> result = medicamentRepo
                .findByUserAndNameContainingIgnoreCaseOrderByIdAsc(user, "AMO");

        assertThat(result).extracting(Medicament::getName)
                .containsExactly("Amoxicillin");
    }
}
