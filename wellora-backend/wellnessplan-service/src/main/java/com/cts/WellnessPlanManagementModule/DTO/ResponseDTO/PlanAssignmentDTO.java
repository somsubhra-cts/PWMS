package com.cts.WellnessPlanManagementModule.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PlanAssignmentDTO
{
    private Long assignmentId ;
    private Long  patientId;
    private Long planId;       // extracted from plan entity
    private String  planName;     // extracted from plan entity
    private LocalDate assignedDate;
    private String status;
}
