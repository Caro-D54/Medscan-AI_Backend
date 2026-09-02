package com.medscan.app_med.repository;

import com.medscan.app_med.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    void findByEmailReturnsUserWhenPresent() {
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");
        userRepo.save(user);

        Optional<User> result = userRepo.findByEmail("caro@medscan.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("caro@medscan.com");
    }

    @Test
    void findByEmailReturnsEmptyWhenAbsent() {
        Optional<User> result = userRepo.findByEmail("ghost@medscan.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmailReflectsPersistence() {
        User user = new User();
        user.setEmail("caro@medscan.com");
        user.setPassword("hashed");
        user.setName("Carolina");
        userRepo.save(user);

        assertThat(userRepo.existsByEmail("caro@medscan.com")).isTrue();
        assertThat(userRepo.existsByEmail("ghost@medscan.com")).isFalse();
    }
}
