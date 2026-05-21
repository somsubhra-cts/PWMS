package com.pwms.notification.controller;

import com.pwms.notification.dto.NotificationDTO;
import com.pwms.notification.exception.NotificationNotFoundException;
import com.pwms.notification.interfaces.NotificationIntf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification",
        description = "Manage patient and admin notifications and alerts")
public class NotificationController {

    private final NotificationIntf notificationService;

    @Operation(summary = "Get all notifications for a patient")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications found"),
            @ApiResponse(responseCode = "404", description = "No notifications found")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NotificationDTO>> getForPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId)
            throws NotificationNotFoundException {
        return ResponseEntity.ok(
                notificationService.getNotificationsForPatient(patientId));
    }

    @Operation(summary = "Get unread notifications for a patient",
            description = "Returns only notifications where isRead is false")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notifications found"),
            @ApiResponse(responseCode = "404", description = "No unread notifications")
    })
    @GetMapping("/patient/{patientId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadForPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId)
            throws NotificationNotFoundException {
        return ResponseEntity.ok(
                notificationService.getUnreadForPatient(patientId));
    }

    @Operation(summary = "Get all notifications for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications found"),
            @ApiResponse(responseCode = "404", description = "No notifications found")
    })
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<NotificationDTO>> getForAdmin(
            @Parameter(description = "Admin ID", required = true)
            @PathVariable int adminId)
            throws NotificationNotFoundException {
        return ResponseEntity.ok(
                notificationService.getNotificationsForAdmin(adminId));
    }

    @Operation(summary = "Get unread notifications for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notifications found"),
            @ApiResponse(responseCode = "404", description = "No unread notifications")
    })
    @GetMapping("/admin/{adminId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadForAdmin(
            @Parameter(description = "Admin ID", required = true)
            @PathVariable int adminId)
            throws NotificationNotFoundException {
        return ResponseEntity.ok(
                notificationService.getUnreadForAdmin(adminId));
    }

    @Operation(summary = "Mark notification as read",
            description = "Sets isRead to true for the given notification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable int notificationId)
            throws NotificationNotFoundException {
        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId));
    }
}
