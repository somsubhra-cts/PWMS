package com.pwms.progress.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponseDTO {
    private int       progressId;
    private int       patientId;
    private int       planId;
    private int       activityId;
    private String    activityName;
    private String    status;
    private LocalDate trackedDate;
}