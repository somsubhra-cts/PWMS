package com.pwms.notification.dto;

import com.pwms.notification.model.Notification.NotificationType;
import com.pwms.notification.model.Notification.ReceiverType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {
    private int              receiverId;
    private ReceiverType     receiverType;
    private NotificationType notificationType;
    private Integer          patientId;
    private Integer          planId;
}