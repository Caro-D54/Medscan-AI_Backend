package com.medscan.app_med.security;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import com.medscan.app_med.repository.UserRepo;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserRepo userRepo = mock(UserRepo.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepo);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesWhenBearerTokenIsValidAndAssignsUserRole() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("caro@medscan.com");
        user.setRole(Role.USER);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("valid-token")).thenReturn("caro@medscan.com");
        when(userRepo.findByEmail("caro@medscan.com")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(user);
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authenticatesWhenBearerTokenIsValidAndAssignsAdminRole() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setEmail("admin@medscan.com");
        user.setRole(Role.ADMIN);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer admin-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("admin-token")).thenReturn("admin@medscan.com");
        when(userRepo.findByEmail("admin@medscan.com")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(user);
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void leavesContextEmptyWhenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractEmail(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void leavesContextEmptyWhenHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
