package com.pwms.notification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationId;

    // Who receives it — plain integer refs, no cross-module JPA join
    @Column(name = "receiver_id", nullable = false)
    private int receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_type", nullable = false)
    private ReceiverType receiverType;          // PATIENT or ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false,length = 50)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String message;

    // Context references — which plan/patient triggered this
    @Column(name = "patient_id")
    private Integer patientId;

    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────

    public enum ReceiverType {
        PATIENT, ADMIN
    }

    public enum NotificationType {
        // Patient-facing
        ACTIVITY_REMINDER,          // activity still PENDING
        ACTIVITY_APPRECIATION,      // activity marked DONE
        WEEKLY_SUMMARY,             // 7-day cycle ends
        REPORT_SHARED,              // admin finalized report
        APPOINTMENT_REMINDER,       // plan COMPLETED → see doctor
        PLAN_ASSIGNED,

        // Admin-facing
        NEW_PATIENT_REGISTERED,     // new patient added
        PLAN_COMPLETED,             // patient completed all activities
        GENERATE_REPORT_REMINDER    // admin needs to generate report
    }
}