package com.pwms.notification.unit;

import com.pwms.notification.dto.NotificationDTO;
import com.pwms.notification.exception.NotificationNotFoundException;
import com.pwms.notification.model.Notification;
import com.pwms.notification.model.Notification.NotificationType;
import com.pwms.notification.model.Notification.ReceiverType;
import com.pwms.notification.repository.NotificationRepository;
import com.pwms.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notifRepo;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setNotificationId(1);
        notification.setReceiverId(1);
        notification.setReceiverType(ReceiverType.PATIENT);
        notification.setNotificationType(NotificationType.ACTIVITY_APPRECIATION);
        notification.setMessage("Great job completing Walking!");
        notification.setPatientId(1);
        notification.setPlanId(1);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
    }

    // ── notifyActivityAppreciation ────────────────────────────

    @Test
    void notifyActivityAppreciation_savesNotification() {
        when(notifRepo.save(any(Notification.class)))
                .thenReturn(notification);

        assertDoesNotThrow(() ->
                notificationService.notifyActivityAppreciation(1, 1, "Walking"));

        verify(notifRepo, times(1)).save(any(Notification.class));
    }

    // ── notifyNewPatientRegistered ────────────────────────────

    @Test
    void notifyNewPatientRegistered_savesAdminNotification() {
        when(notifRepo.save(any(Notification.class)))
                .thenReturn(notification);

        assertDoesNotThrow(() ->
                notificationService.notifyNewPatientRegistered(1, 1));

        verify(notifRepo, times(1)).save(any(Notification.class));
    }

    // ── notifyPlanCompleted ───────────────────────────────────

    @Test
    void notifyPlanCompleted_savesTwoNotifications() {
        when(notifRepo.save(any(Notification.class)))
                .thenReturn(notification);

        assertDoesNotThrow(() ->
                notificationService.notifyPlanCompleted(1, 1, 1));

        // One for patient + one for admin
        verify(notifRepo, times(2)).save(any(Notification.class));
    }

    // ── getNotificationsForPatient ────────────────────────────

    @Test
    void getNotificationsForPatient_success()
            throws NotificationNotFoundException {
        when(notifRepo.findByReceiverIdAndReceiverType(1, ReceiverType.PATIENT))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result =
                notificationService.getNotificationsForPatient(1);

        assertEquals(1, result.size());
        assertEquals("Great job completing Walking!",
                result.get(0).getMessage());
    }

    @Test
    void getNotificationsForPatient_notFound_throwsException() {
        when(notifRepo.findByReceiverIdAndReceiverType(99, ReceiverType.PATIENT))
                .thenReturn(List.of());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.getNotificationsForPatient(99));
    }

    // ── markAsRead ────────────────────────────────────────────

    @Test
    void markAsRead_success() throws NotificationNotFoundException {
        when(notifRepo.findById(1)).thenReturn(Optional.of(notification));
        notification.setRead(true);
        when(notifRepo.save(any(Notification.class)))
                .thenReturn(notification);

        NotificationDTO result = notificationService.markAsRead(1);

        assertTrue(result.isRead());
        verify(notifRepo, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_notFound_throwsException() {
        when(notifRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(99));
    }

    // ── getUnreadForPatient ───────────────────────────────────

    @Test
    void getUnreadForPatient_success()
            throws NotificationNotFoundException {
        when(notifRepo.findByReceiverIdAndReceiverTypeAndIsRead(
                1, ReceiverType.PATIENT, false))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result =
                notificationService.getUnreadForPatient(1);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }
}