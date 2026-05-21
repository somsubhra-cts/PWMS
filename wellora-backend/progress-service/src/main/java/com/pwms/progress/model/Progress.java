package com.pwms.progress.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "progress",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"patient_id", "activity_id", "tracked_date"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int progressId;

    @Column(name = "patient_id", nullable = false)
    private int patientId;

    @Column(name = "plan_id", nullable = false)
    private int planId;

    @Column(name = "activity_id", nullable = false)
    private int activityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    @Column(name = "tracked_date", nullable = false)
    private LocalDate trackedDate;

    public enum ActivityStatus {
        DONE, PENDING, SKIPPED
    }
}