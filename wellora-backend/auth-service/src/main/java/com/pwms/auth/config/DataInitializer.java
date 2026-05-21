package com.pwms.auth.config;

import com.pwms.auth.model.User;
import com.pwms.auth.model.User.Role;
import com.pwms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdminData() {
        return args -> {

            // ── Admin 1 ───────────────────────────────────────
            if (!userRepo.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setReferenceId(1);
                userRepo.save(admin);
                log.info("Admin user created — username: admin");
            } else {
                log.info("Admin user already exists — skipping");
            }

            // ── Admin 2 — doctor ──────────────────────────────
            if (!userRepo.existsByUsername("doctor")) {
                User doctor = new User();
                doctor.setUsername("doctor");
                doctor.setPassword(passwordEncoder.encode("doctor123"));
                doctor.setRole(Role.ADMIN);
                doctor.setReferenceId(2);
                userRepo.save(doctor);
                log.info("Doctor user created — username: doctor");
            } else {
                log.info("Doctor user already exists — skipping");
            }

        };
    }
}