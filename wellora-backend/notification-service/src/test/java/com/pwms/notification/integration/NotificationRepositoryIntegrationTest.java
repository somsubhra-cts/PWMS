package com.pwms.notification.integration;

import com.pwms.notification.model.Notification;
import com.pwms.notification.model.Notification.NotificationType;
import com.pwms.notification.model.Notification.ReceiverType;
import com.pwms.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false"
})
class NotificationRepositoryIntegrationTest {

    @Autowired
    private NotificationRepository notifRepo;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notifRepo.deleteAll();

        notification = new Notification();
        notification.setReceiverId(1);
        notification.setReceiverType(ReceiverType.PATIENT);
        notification.setNotificationType(NotificationType.ACTIVITY_APPRECIATION);
        notification.setMessage("Great job completing Walking!");
        notification.setPatientId(1);
        notification.setPlanId(1);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save_andFindByReceiverIdAndType() {
        notifRepo.save(notification);

        List<Notification> result =
                notifRepo.findByReceiverIdAndReceiverType(1, ReceiverType.PATIENT);

        assertEquals(1, result.size());
        assertEquals("Great job completing Walking!",
                result.get(0).getMessage());
    }

    @Test
    void findUnread_returnsOnlyUnread() {
        notifRepo.save(notification);

        Notification read = new Notification();
        read.setReceiverId(1);
        read.setReceiverType(ReceiverType.PATIENT);
        read.setNotificationType(NotificationType.ACTIVITY_REMINDER);
        read.setMessage("Complete Yoga today");
        read.setRead(true);
        read.setCreatedAt(LocalDateTime.now());
        notifRepo.save(read);

        List<Notification> unread =
                notifRepo.findByReceiverIdAndReceiverTypeAndIsRead(
                        1, ReceiverType.PATIENT, false);

        assertEquals(1, unread.size());
        assertFalse(unread.get(0).isRead());
    }

    @Test
    void findAdminNotifications() {
        Notification adminNotif = new Notification();
        adminNotif.setReceiverId(1);
        adminNotif.setReceiverType(ReceiverType.ADMIN);
        adminNotif.setNotificationType(NotificationType.NEW_PATIENT_REGISTERED);
        adminNotif.setMessage("New patient registered");
        adminNotif.setRead(false);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notifRepo.save(adminNotif);

        List<Notification> result =
                notifRepo.findByReceiverIdAndReceiverType(1, ReceiverType.ADMIN);

        assertEquals(1, result.size());
        assertEquals(ReceiverType.ADMIN,
                result.get(0).getReceiverType());
    }

    @Test
    void findByPatientIdAndType() {
        notifRepo.save(notification);

        List<Notification> result =
                notifRepo.findByPatientIdAndNotificationType(
                        1, NotificationType.ACTIVITY_APPRECIATION);

        assertEquals(1, result.size());
    }

    @Test
    void markAsRead_updatesStatus() {
        Notification saved = notifRepo.save(notification);
        saved.setRead(true);
        notifRepo.save(saved);

        Notification updated =
                notifRepo.findById(saved.getNotificationId()).orElseThrow();
        assertTrue(updated.isRead());
    }
}