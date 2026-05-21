package com.pwms.auth.repository;

import com.pwms.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    // Checks if a patient account already exists — prevents duplicate accounts per patient
    boolean existsByReferenceId(Integer referenceId);
}