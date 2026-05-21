package com.pwms.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private int           notificationId;
    private int           receiverId;
    private String        receiverType;
    private String        notificationType;
    private String        message;
    private Integer       patientId;
    private Integer       planId;
    @JsonProperty("isRead")
    private boolean       isRead;
    private LocalDateTime createdAt;
}
