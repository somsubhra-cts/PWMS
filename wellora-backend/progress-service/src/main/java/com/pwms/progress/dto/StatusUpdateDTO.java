package com.pwms.progress.dto;

import com.pwms.progress.model.Progress.ActivityStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDTO {

    @Min(value = 1, message = "Plan ID must be greater than 0")
    private int planId;

    @Min(value = 1, message = "Activity ID must be greater than 0")
    private int activityId;

    @NotNull(message = "Status is required — must be DONE, PENDING or SKIPPED")
    private ActivityStatus status;

    public StatusUpdateDTO(int i, ActivityStatus activityStatus) {
    }
}