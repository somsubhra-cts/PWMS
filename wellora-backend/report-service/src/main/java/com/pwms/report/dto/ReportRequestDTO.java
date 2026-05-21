package com.pwms.report.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    @Min(value = 1, message = "Admin ID must be greater than 0")
    private int adminId;

    @NotBlank(message = "Admin summary is required")
    @Size(min = 10, max = 1000,
            message = "Summary must be between 10 and 1000 characters")
    private String adminSummary;
}