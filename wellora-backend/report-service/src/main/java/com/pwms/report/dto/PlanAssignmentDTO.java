package com.pwms.report.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanAssignmentDTO {
    private int       assignmentId;
    private int       patientId;
//    private PlanDTO   plan;

    // completed code
    private int planId;
    private String planName;
    private LocalDate assignedDate;
    private String    status;           // ACTIVE, COMPLETED, CANCELLED
}