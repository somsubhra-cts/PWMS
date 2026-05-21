package com.pwms.progress.service;

import com.pwms.progress.client.NotificationClient;
import com.pwms.progress.client.WellnessPlanClient;
import com.pwms.progress.dto.*;
import com.pwms.progress.exception.*;
import com.pwms.progress.interfaces.ProgressIntf;
import com.pwms.progress.model.Progress;
import com.pwms.progress.model.Progress.ActivityStatus;
import com.pwms.progress.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService implements ProgressIntf {

    private final ProgressRepository progressRepo;
    private final WellnessPlanClient  planClient;
    private final NotificationClient  notificationClient;

    private static final int DEFAULT_ADMIN_ID = 1;

    @Override
    public void initializeProgress(int patientId, int planId)
            throws ProgressAlreadyExistsException {
        log.debug("Initializing progress for patientId: {} planId: {}",
                patientId, planId);

        List<Progress> existing =
                progressRepo.findByPatientIdAndPlanId(patientId, planId);
        if (!existing.isEmpty()) {
            log.warn("Progress already initialized for patientId: {} planId: {}",
                    patientId, planId);
            throw new ProgressAlreadyExistsException(
                    "Progress already initialized for patientId: " + patientId +
                            " planId: " + planId);
        }

        List<ActivityDTO> activities = planClient.getActivitiesByPlanId(planId);
        log.debug("Fetched {} activities for planId: {}",
                activities.size(), planId);

        List<Progress> seedRows = activities.stream()
                .map(a -> Progress.builder()
                        .patientId(patientId)
                        .planId(planId)
                        .activityId(a.getActivityId())
                        .status(ActivityStatus.PENDING)
                        .trackedDate(LocalDate.now())
                        .build())
                .collect(Collectors.toList());

        progressRepo.saveAll(seedRows);
        log.info("Progress initialized — patientId: {} planId: {} rows: {}",
                patientId, planId, seedRows.size());
    }

    @Override
    public List<ProgressResponseDTO> seedProgressForDate(int patientId, int planId) {
        log.debug("Seed requested — patientId: {} planId: {} date: {}",
                patientId, planId, LocalDate.now());

        List<Progress> existing = progressRepo
                .findByPatientIdAndPlanIdAndTrackedDate(patientId, planId, LocalDate.now());
        if (!existing.isEmpty()) {
            log.info("Progress already exists for today — returning existing rows. patientId: {} planId: {} date: {}",
                    patientId, planId, LocalDate.now());
            return enrichWithActivityNames(existing);
        }

        List<ActivityDTO> activities = planClient.getActivitiesByPlanId(planId);
        List<Progress> newRows = activities.stream()
                .map(a -> Progress.builder()
                        .patientId(patientId)
                        .planId(planId)
                        .activityId(a.getActivityId())
                        .status(ActivityStatus.PENDING)
                        .trackedDate(LocalDate.now())
                        .build())
                .collect(Collectors.toList());

        List<Progress> saved = progressRepo.saveAll(newRows);
        log.info("Seeded {} PENDING rows — patientId: {} planId: {} date: {}",
                saved.size(), patientId, planId, LocalDate.now());

        return enrichWithActivityNames(saved);
    }

    @Override
    public ProgressResponseDTO updateStatus(int patientId, StatusUpdateDTO dto)
            throws ProgressNotFoundException {
        log.debug("Updating status for patientId: {} activityId: {} status: {}",
                patientId, dto.getActivityId(), dto.getStatus());

        List<Progress> allRows = progressRepo.findByPatientId(patientId);
        log.info("DEBUG updateStatus — patientId: {} activityId: {} — total rows in DB for patient: {}",
                patientId, dto.getActivityId(), allRows.size());
        allRows.forEach(r -> log.info("  DB row → progressId:{} patientId:{} planId:{} activityId:{} status:{} date:{}",
                r.getProgressId(), r.getPatientId(), r.getPlanId(),
                r.getActivityId(), r.getStatus(), r.getTrackedDate()));

        seedTodayIfNeeded(patientId, dto.getPlanId());

        Progress p = progressRepo
                .findFirstByPatientIdAndActivityIdOrderByTrackedDateDesc(
                        patientId, dto.getActivityId())
                .orElseThrow(() -> {
                    log.warn("Progress row not found for patientId: {} activityId: {} — available activityIds: {}",
                            patientId, dto.getActivityId(),
                            allRows.stream().map(Progress::getActivityId).toList());
                    return new ProgressNotFoundException(
                            "No progress row found for patientId: " + patientId +
                                    " activityId: " + dto.getActivityId());
                });

        p.setStatus(dto.getStatus());
        p.setTrackedDate(LocalDate.now());
        Progress saved = progressRepo.save(p);
        log.info("Activity status updated — patientId: {} activityId: {} status: {}",
                patientId, dto.getActivityId(), dto.getStatus());

        String activityName = getActivityName(saved.getPlanId(),
                saved.getActivityId());

        if (dto.getStatus() == ActivityStatus.DONE) {
            triggerSafely("activity appreciation",
                    () -> notificationClient.notifyActivityAppreciation(
                            patientId, saved.getPlanId(), activityName));

            if (isAllActivitiesDone(patientId, saved.getPlanId())) {
                log.info("All activities done — patientId: {} planId: {}",
                        patientId, saved.getPlanId());

                triggerSafely("plan completed",
                        () -> notificationClient.notifyPlanCompleted(
                                patientId, saved.getPlanId(), DEFAULT_ADMIN_ID));

                triggerSafely("appointment reminder",
                        () -> notificationClient.notifyAppointmentReminder(
                                patientId, saved.getPlanId()));

                ProgressSummaryDTO summary =
                        getDailySummary(patientId, saved.getPlanId());
                triggerSafely("weekly summary",
                        () -> notificationClient.notifyWeeklySummary(
                                patientId, saved.getPlanId(),
                                summary.getCompletionPercentage()));
            }
        } else if (dto.getStatus() == ActivityStatus.PENDING) {
            triggerSafely("activity reminder",
                    () -> notificationClient.notifyActivityReminder(
                            patientId, saved.getPlanId(), activityName));
        }

        return toDTO(saved, activityName);
    }

    @Override
    public List<ProgressResponseDTO> getProgressByPatient(int patientId)
            throws ProgressNotFoundException {
        log.debug("Fetching progress for patientId: {}", patientId);
        List<Progress> existing = progressRepo.findByPatientId(patientId);
        if (existing.isEmpty()) {
            log.warn("No progress found for patientId: {}", patientId);
            throw new ProgressNotFoundException(
                    "No progress found for patientId: " + patientId);
        }

        existing.stream()
                .map(Progress::getPlanId)
                .distinct()
                .forEach(planId -> seedTodayIfNeeded(patientId, planId));

        List<Progress> records = progressRepo.findByPatientId(patientId);
        return enrichWithActivityNames(records);
    }

    @Override
    public List<ProgressResponseDTO> getProgressByPatientAndPlan(
            int patientId, int planId) throws ProgressNotFoundException {
        log.debug("Fetching progress for patientId: {} planId: {}",
                patientId, planId);
        seedTodayIfNeeded(patientId, planId);
        List<Progress> records =
                progressRepo.findByPatientIdAndPlanId(patientId, planId);
        if (records.isEmpty()) {
            log.warn("No progress found for patientId: {} planId: {}",
                    patientId, planId);
            throw new ProgressNotFoundException(
                    "No progress found for patientId: " + patientId +
                            " planId: " + planId);
        }
        return enrichWithActivityNames(records);
    }

    @Override
    public ProgressSummaryDTO getDailySummary(int patientId, int planId)
                throws ProgressNotFoundException {
            log.debug("Calculating daily summary for patientId: {} planId: {}",
                patientId, planId);

        List<ActivityDTO> activities = planClient.getActivitiesByPlanId(planId);
        int total = activities.size();
        long completed = progressRepo.countByStatusForDate(
                patientId, planId, ActivityStatus.DONE, LocalDate.now());

        double pct = total > 0
                ? Math.round((completed * 100.0 / total) * 100.0) / 100.0 : 0.0;

        log.debug("Summary — patientId: {} total: {} completed: {} pct: {}%",
                patientId, total, completed, pct);

        return ProgressSummaryDTO.builder()
                .patientId(patientId).planId(planId)
                .date(LocalDate.now())
                .totalActivities(total)
                .completedActivities((int) completed)
                .completionPercentage(pct)
                .build();
    }

    @Override
    public List<ProgressResponseDTO> getProgressByPatientAndPlanAndDate(
            int patientId, int planId, LocalDate date)
            throws ProgressNotFoundException {
        log.debug("Fetching progress for patientId: {} planId: {} date: {}",
                patientId, planId, date);
        seedTodayIfNeeded(patientId, planId);
        List<Progress> records =
                progressRepo.findByPatientIdAndPlanIdAndTrackedDate(
                        patientId, planId, date);
        if (records.isEmpty()) {
            log.warn("No progress found for patientId: {} planId: {} date: {}",
                    patientId, planId, date);
            throw new ProgressNotFoundException(
                    "No progress found for patientId: " + patientId +
                            " planId: " + planId + " date: " + date);
        }
        return enrichWithActivityNames(records);
    }

    @Override
    public List<ProgressResponseDTO> getProgressByPatientAndDate(
            int patientId, LocalDate date)
            throws ProgressNotFoundException {
        log.debug("Fetching progress for patientId: {} date: {}", patientId, date);

        if (date.equals(LocalDate.now())) {
            progressRepo.findByPatientId(patientId).stream()
                    .map(Progress::getPlanId)
                    .distinct()
                    .forEach(planId -> seedTodayIfNeeded(patientId, planId));
        }

        List<Progress> records =
                progressRepo.findByPatientIdAndTrackedDateOrderByProgressIdAsc(patientId, date);
        if (records.isEmpty()) {
            log.warn("No progress found for patientId: {} date: {}", patientId, date);
            throw new ProgressNotFoundException(
                    "No progress found for patientId: " + patientId + " date: " + date);
        }
        return enrichWithActivityNames(records);
    }


    private void seedTodayIfNeeded(int patientId, int planId) {
        try {
            seedProgressForDate(patientId, planId);
        } catch (Exception e) {
            log.error("Auto-seed failed for patientId: {} planId: {}: {}",
                    patientId, planId, e.getMessage());
        }
    }

    private boolean isAllActivitiesDone(int patientId, int planId) {
        List<Progress> todayRecords =
                progressRepo.findByPatientIdAndPlanIdAndTrackedDateBetween(
                        patientId, planId, LocalDate.now(), LocalDate.now());
        return !todayRecords.isEmpty() &&
                todayRecords.stream()
                        .allMatch(p -> p.getStatus() == ActivityStatus.DONE);
    }

    private List<ProgressResponseDTO> enrichWithActivityNames(
            List<Progress> records) {
        if (records.isEmpty()) return List.of();
        int planId = records.get(0).getPlanId();
        Map<Integer, String> nameMap;
        try {
            nameMap = planClient.getActivitiesByPlanId(planId).stream()
                    .collect(Collectors.toMap(
                            ActivityDTO::getActivityId,
                            ActivityDTO::getActivityName));
        } catch (Exception e) {
            log.error("Failed to fetch activity names for planId: {} — {}",
                    planId, e.getMessage());
            nameMap = Map.of();
        }
        final Map<Integer, String> finalMap = nameMap;
        return records.stream()
                .map(p -> toDTO(p,
                        finalMap.getOrDefault(p.getActivityId(), "Unknown")))
                .collect(Collectors.toList());
    }

    private String getActivityName(int planId, int activityId) {
        try {
            return planClient.getActivitiesByPlanId(planId).stream()
                    .filter(a -> a.getActivityId() == activityId)
                    .map(ActivityDTO::getActivityName)
                    .findFirst().orElse("Unknown");
        } catch (Exception e) {
            log.error("Failed to get activity name — planId: {} activityId: {}",
                    planId, activityId);
            return "Unknown";
        }
    }

    private void triggerSafely(String type, Runnable action) {
        try {
            action.run();
            log.info("Notification triggered successfully: {}", type);
        } catch (Exception e) {
            log.error("Failed to trigger '{}' notification — [{}]: {}",
                    type, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private ProgressResponseDTO toDTO(Progress p, String activityName) {
        return ProgressResponseDTO.builder()
                .progressId(p.getProgressId())
                .patientId(p.getPatientId())
                .planId(p.getPlanId())
                .activityId(p.getActivityId())
                .activityName(activityName)
                .status(p.getStatus().name())
                .trackedDate(p.getTrackedDate())
                .build();
    }
}