package com.pwms.report.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportPreviewDTO {
    private int    patientId;
    private String patientName;
    private int    planId;
    private String planName;
    private int    totalActivities;
    private int    completedActivities;
    private double completionPercentage;
    private String dateRange;           // e.g. "2026-03-27 to 2026-04-03"
    // Admin fills this in before submitting
   // private String adminSummary;
}