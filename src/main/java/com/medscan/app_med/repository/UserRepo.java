package com.medscan.app_med.repository;

import com.medscan.app_med.model.Role;
import com.medscan.app_med.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    long countByRole(Role role);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isLoggedIn = true")
    long countByIsLoggedInTrue();

    @Query("SELECT u FROM User u WHERE u.isLoggedIn = true")
    Page<User> findByIsLoggedInTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isLoggedIn = true AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> findByIsLoggedInTrueAndSearch(@Param("query") String query, Pageable pageable);
}
