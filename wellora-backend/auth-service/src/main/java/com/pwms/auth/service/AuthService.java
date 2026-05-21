package com.pwms.auth.service;

import com.pwms.auth.client.PatientClient;
import com.pwms.auth.client.PatientClient.PatientVerifyDTO;
import com.pwms.auth.dto.*;
import com.pwms.auth.model.User;
import com.pwms.auth.repository.UserRepository;
import com.pwms.auth.security.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;
    private final PatientClient   patientClient;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthResponseDTO register(RegisterRequestDTO request) {

        // 3-layer identity check for patient self-registration
        if (request.getRole() == User.Role.PATIENT) {

            if (request.getReferenceId() == null) {
                throw new RuntimeException("Patient ID is required for patient registration.");
            }

            if (request.getPatientEmail() == null || request.getPatientEmail().isBlank()) {
                throw new RuntimeException("Registered email is required to verify your identity.");
            }

            // Layer 1: one account per patient
            if (userRepo.existsByReferenceId(request.getReferenceId())) {
                throw new RuntimeException(
                        "An account already exists for Patient ID " + request.getReferenceId() +
                        ". Please contact your admin if you have forgotten your credentials.");
            }

            // Layer 2: patient must exist in patient-service
            PatientVerifyDTO patient;
            try {
                patient = patientClient.getPatientById(request.getReferenceId());
            } catch (FeignException.NotFound e) {
                log.warn("Registration attempt with non-existent Patient ID: {}",
                        request.getReferenceId());
                throw new RuntimeException(
                        "Patient ID " + request.getReferenceId() +
                        " does not exist. Please check the ID provided by your admin.");
            } catch (Exception e) {
                log.error("Could not reach patient-service during registration: {}",
                        e.getMessage());
                throw new RuntimeException(
                        "Unable to verify your Patient ID right now. Please try again later.");
            }

            // Layer 3: email must match the admin-registered record
            if (!patient.getEmail().equalsIgnoreCase(request.getPatientEmail().trim())) {
                log.warn("Email mismatch for Patient ID {}: provided='{}' expected='{}'",
                        request.getReferenceId(), request.getPatientEmail(), patient.getEmail());
                throw new RuntimeException(
                        "The email you entered does not match our records for Patient ID " +
                        request.getReferenceId() +
                        ". Please use the email your doctor registered for you.");
            }

            log.info("Patient identity verified — patientId: {} name: '{}'",
                    patient.getPatientId(), patient.getPatientName());
        }

        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException(
                    "Username '" + request.getUsername() +
                    "' is already taken. Please choose another.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setReferenceId(request.getReferenceId());

        userRepo.save(user);
        log.info("New user registered — username: '{}' role: {} referenceId: {}",
                user.getUsername(), user.getRole(), user.getReferenceId());

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getReferenceId()
        );

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .referenceId(user.getReferenceId())
                .expiresIn(expiration)
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getReferenceId()
        );

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .referenceId(user.getReferenceId())
                .expiresIn(expiration)
                .build();
    }

    // called by the gateway filter
    public boolean validate(String token) {
        return jwtUtil.validateToken(token);
    }

    // called by the gateway filter
    public String extractRole(String token) {
        return jwtUtil.extractRole(token);
    }

    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }
}
