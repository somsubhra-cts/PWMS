package com.pwms.notification.repository;

import com.pwms.notification.model.Notification;
import com.pwms.notification.model.Notification.NotificationType;
import com.pwms.notification.model.Notification.ReceiverType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // All notifications for a receiver (patient or admin)
    List<Notification> findByReceiverIdAndReceiverType(
            int receiverId, ReceiverType receiverType);

    // Unread notifications only
    List<Notification> findByReceiverIdAndReceiverTypeAndIsRead(
            int receiverId, ReceiverType receiverType, boolean isRead);

    // All notifications of a specific type for a patient
    List<Notification> findByPatientIdAndNotificationType(
            int patientId, NotificationType type);

    // All unread admin notifications
    List<Notification> findByReceiverTypeAndIsRead(
            ReceiverType receiverType, boolean isRead);
}