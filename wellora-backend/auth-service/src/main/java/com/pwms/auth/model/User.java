package com.pwms.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // stored as BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Links to patient or admin ID in their respective service
    // unique = true ensures one account per patient — prevents account hijacking
    @Column(name = "reference_id", unique = true)
    private Integer referenceId;

    public enum Role {
        ADMIN, PATIENT
    }
}