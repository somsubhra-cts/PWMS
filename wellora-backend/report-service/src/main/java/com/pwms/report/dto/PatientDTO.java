package com.pwms.report.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientDTO {
    private int    patientId;
    private String patientName;
    private int    age;
    private String email;
    private String medicalHistory;
}

