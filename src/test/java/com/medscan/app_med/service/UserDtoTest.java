package com.medscan.app_med.service;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    @Test
    void fromEntityReturnsUserDtoWithMatchingFields() {
        User user = new User();
        user.setId(42L);
        user.setName("Carolina");
        user.setEmail("caro@medscan.com");
        user.setRole(Role.ADMIN);

        UserDto dto = UserDto.fromEntity(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.name()).isEqualTo("Carolina");
        assertThat(dto.email()).isEqualTo("caro@medscan.com");
        assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void fromEntityDefaultsRoleToUserWhenNullOnEntity() {
        User user = new User();
        user.setId(10L);
        user.setName("Paciente");
        user.setEmail("paciente@medscan.com");
        user.setRole(null);

        UserDto dto = UserDto.fromEntity(user);

        assertThat(dto).isNotNull();
        assertThat(dto.role()).isEqualTo(Role.USER);
    }

    @Test
    void fromEntityReturnsNullWhenEntityIsNull() {
        UserDto dto = UserDto.fromEntity(null);

        assertThat(dto).isNull();
    }
}
