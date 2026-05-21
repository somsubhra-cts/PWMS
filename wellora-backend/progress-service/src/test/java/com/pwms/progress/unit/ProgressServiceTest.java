package com.pwms.progress.unit;

import com.pwms.progress.client.NotificationClient;
import com.pwms.progress.client.WellnessPlanClient;
import com.pwms.progress.dto.*;
import com.pwms.progress.exception.*;
import com.pwms.progress.model.Progress;
import com.pwms.progress.model.Progress.ActivityStatus;
import com.pwms.progress.repository.ProgressRepository;
import com.pwms.progress.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock private ProgressRepository  progressRepo;
    @Mock private WellnessPlanClient  planClient;
    @Mock private NotificationClient  notificationClient;

    @InjectMocks
    private ProgressService progressService;

    private Progress        progress;
    private ActivityDTO     activityDTO;

    @BeforeEach
    void setUp() {
        progress = Progress.builder()
                .progressId(1)
                .patientId(1)
                .planId(1)
                .activityId(1)
                .status(ActivityStatus.PENDING)
                .trackedDate(LocalDate.now())
                .build();

        activityDTO = new ActivityDTO(1, "Walking");
    }

    // ── initializeProgress ────────────────────────────────────

    @Test
    void initializeProgress_success()
            throws ProgressAlreadyExistsException {
        when(progressRepo.findByPatientIdAndPlanId(1, 1))
                .thenReturn(List.of());
        when(planClient.getActivitiesByPlanId(1))
                .thenReturn(List.of(activityDTO));
        when(progressRepo.saveAll(anyList()))
                .thenReturn(List.of(progress));

        assertDoesNotThrow(() ->
                progressService.initializeProgress(1, 1));

        verify(progressRepo, times(1)).saveAll(anyList());
    }

    @Test
    void initializeProgress_alreadyExists_throwsException() {
        when(progressRepo.findByPatientIdAndPlanId(1, 1))
                .thenReturn(List.of(progress));

        assertThrows(ProgressAlreadyExistsException.class,
                () -> progressService.initializeProgress(1, 1));

        verify(progressRepo, never()).saveAll(any());
    }

    // ── updateStatus ──────────────────────────────────────────

    @Test
    void updateStatus_toDone_success()
            throws ProgressNotFoundException {
        StatusUpdateDTO dto = new StatusUpdateDTO(1, ActivityStatus.DONE);

        when(progressRepo.findByPatientIdAndActivityIdAndTrackedDate(
                1, 1, LocalDate.now())).thenReturn(Optional.of(progress));
        when(progressRepo.save(any(Progress.class))).thenReturn(progress);
        when(planClient.getActivitiesByPlanId(1))
                .thenReturn(List.of(activityDTO));
        when(progressRepo.findByPatientIdAndPlanIdAndTrackedDateBetween(
                anyInt(), anyInt(), any(), any()))
                .thenReturn(List.of(progress));

        ProgressResponseDTO result =
                progressService.updateStatus(1, dto);

        assertNotNull(result);
        verify(notificationClient, times(1))
                .notifyActivityAppreciation(eq(1), eq(1), anyString());
    }

    @Test
    void updateStatus_notFound_throwsException() {
        StatusUpdateDTO dto = new StatusUpdateDTO(99, ActivityStatus.DONE);

        when(progressRepo.findByPatientIdAndActivityIdAndTrackedDate(
                1, 99, LocalDate.now())).thenReturn(Optional.empty());

        assertThrows(ProgressNotFoundException.class,
                () -> progressService.updateStatus(1, dto));
    }

    // ── getProgressByPatient ──────────────────────────────────

    @Test
    void getProgressByPatient_success()
            throws ProgressNotFoundException {
        when(progressRepo.findByPatientId(1))
                .thenReturn(List.of(progress));
        when(planClient.getActivitiesByPlanId(1))
                .thenReturn(List.of(activityDTO));

        List<ProgressResponseDTO> result =
                progressService.getProgressByPatient(1);

        assertEquals(1, result.size());
        assertEquals("Walking", result.get(0).getActivityName());
    }

    @Test
    void getProgressByPatient_notFound_throwsException() {
        when(progressRepo.findByPatientId(99))
                .thenReturn(List.of());

        assertThrows(ProgressNotFoundException.class,
                () -> progressService.getProgressByPatient(99));
    }

    // ── getDailySummary ───────────────────────────────────────

    @Test
    void getDailySummary_success()
            throws ProgressNotFoundException {
        when(planClient.getActivitiesByPlanId(1))
                .thenReturn(List.of(activityDTO));
        when(progressRepo.countByStatusForDate(
                1, 1, ActivityStatus.DONE, LocalDate.now()))
                .thenReturn(1L);

        ProgressSummaryDTO result =
                progressService.getDailySummary(1, 1);

        assertEquals(1, result.getTotalActivities());
        assertEquals(1, result.getCompletedActivities());
        assertEquals(100.0, result.getCompletionPercentage());
    }
}