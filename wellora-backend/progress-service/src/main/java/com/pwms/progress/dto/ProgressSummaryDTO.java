package com.pwms.progress.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressSummaryDTO {
    private int       patientId;
    private int       planId;
    private LocalDate date;
    private int       totalActivities;
    private int       completedActivities;
    private double    completionPercentage;
}