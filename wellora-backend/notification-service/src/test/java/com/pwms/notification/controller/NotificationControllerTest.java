package com.pwms.notification.controller;

import com.pwms.notification.dto.NotificationDTO;
import com.pwms.notification.exception.NotificationNotFoundException;
import com.pwms.notification.interfaces.NotificationIntf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false",
        "spring.cloud.config.enabled=false"
})
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean
    private NotificationIntf notificationService;

    private NotificationDTO notificationDTO;

    @BeforeEach
    void setUp() {
        notificationDTO = NotificationDTO.builder()
                .notificationId(1)
                .receiverId(1)
                .receiverType("PATIENT")
                .notificationType("ACTIVITY_APPRECIATION")
                .message("Great job completing Walking!")
                .patientId(1)
                .planId(1)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /api/notifications/patient/{patientId} ────────────

    @Test
    void getForPatient_returns200() throws Exception {
        when(notificationService.getNotificationsForPatient(1))
                .thenReturn(List.of(notificationDTO));

        mockMvc.perform(get("/api/notifications/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message")
                        .value("Great job completing Walking!"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getForPatient_notFound_returns404() throws Exception {
        when(notificationService.getNotificationsForPatient(99))
                .thenThrow(new NotificationNotFoundException(
                        "No notifications found for patientId: 99"));

        mockMvc.perform(get("/api/notifications/patient/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/notifications/patient/{patientId}/unread ─────

    @Test
    void getUnreadForPatient_returns200() throws Exception {
        when(notificationService.getUnreadForPatient(1))
                .thenReturn(List.of(notificationDTO));

        mockMvc.perform(get("/api/notifications/patient/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));
    }

    // ── GET /api/notifications/admin/{adminId} ────────────────

    @Test
    void getForAdmin_returns200() throws Exception {
        NotificationDTO adminDTO = NotificationDTO.builder()
                .notificationId(2)
                .receiverId(1)
                .receiverType("ADMIN")
                .notificationType("NEW_PATIENT_REGISTERED")
                .message("New patient registered")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.getNotificationsForAdmin(1))
                .thenReturn(List.of(adminDTO));

        mockMvc.perform(get("/api/notifications/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverType").value("ADMIN"));
    }

    // ── PATCH /api/notifications/{id}/read ────────────────────

    @Test
    void markAsRead_returns200() throws Exception {
        notificationDTO.setRead(true);
        when(notificationService.markAsRead(1))
                .thenReturn(notificationDTO);

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAsRead_notFound_returns404() throws Exception {
        when(notificationService.markAsRead(99))
                .thenThrow(new NotificationNotFoundException(
                        "Notification not found with id: 99"));

        mockMvc.perform(patch("/api/notifications/99/read"))
                .andExpect(status().isNotFound());
    }
}