package com.pwms.progress.repository;

import com.pwms.progress.model.Progress;
import com.pwms.progress.model.Progress.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Integer> {

    List<Progress> findByPatientId(int patientId);

    List<Progress> findByPatientIdAndPlanId(int patientId, int planId);

    Optional<Progress> findByPatientIdAndActivityIdAndTrackedDate(
            int patientId, int activityId, LocalDate trackedDate);

    List<Progress> findByPatientIdAndPlanIdAndTrackedDate(
            int patientId, int planId, LocalDate trackedDate);

    List<Progress> findByPatientIdAndTrackedDateOrderByProgressIdAsc(
            int patientId, LocalDate trackedDate);

    Optional<Progress> findFirstByPatientIdAndActivityIdOrderByTrackedDateDesc(
            int patientId, int activityId);

    List<Progress> findByPatientIdAndPlanIdAndTrackedDateBetween(
            int patientId, int planId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.patientId = :patientId " +
            "AND p.planId = :planId AND p.status = :status " +
            "AND p.trackedDate = :date")
    long countByStatusForDate(
            @Param("patientId") int patientId,
            @Param("planId")    int planId,
            @Param("status")    ActivityStatus status,
            @Param("date")      LocalDate date);
}