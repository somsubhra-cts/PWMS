package com.pwms.report.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private int       reportId;
    private int       patientId;
    private String    patientName;
    private int       planId;
    private String    planName;
    private int       generatedBy;        // adminId
    private String    summary;            // auto-built system summary
    private String    adminSummary;       // written by admin — visible to patient
    private LocalDate date;
    private String    status;             // DRAFT or PUBLISHED
    private int       totalActivities;
    private int       completedActivities;
    private double    completionPercentage;
}