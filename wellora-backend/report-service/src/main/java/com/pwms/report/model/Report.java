package com.pwms.report.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reportId;

    // Plain integer refs — no cross-module JPA joins
    @Column(name = "patient_id", nullable = false)
    private int patientId;

    @Column(name = "plan_id", nullable = false)
    private int planId;

    // Admin who generated the report
    @Column(name = "generated_by", nullable = false)
    private int generatedBy;

    // Auto-built from progress data
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    // Written by admin during report generation
    @Column(name = "admin_summary", columnDefinition = "TEXT")
    private String adminSummary;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @PrePersist
    protected void onCreate() {
        date = LocalDate.now();
        if (status == null) status = ReportStatus.DRAFT;
    }

    public enum ReportStatus {
        DRAFT,      // admin is working on it
        PUBLISHED   // patient can see it
    }
}