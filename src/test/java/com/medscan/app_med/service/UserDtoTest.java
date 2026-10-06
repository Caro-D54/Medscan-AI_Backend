package com.medscan.app_med.service;

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

        UserDto dto = UserDto.fromEntity(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.name()).isEqualTo("Carolina");
        assertThat(dto.email()).isEqualTo("caro@medscan.com");
    }

    @Test
    void fromEntityReturnsNullWhenEntityIsNull() {
        UserDto dto = UserDto.fromEntity(null);

        assertThat(dto).isNull();
    }
}
