package com.pwms.report.repository;

import com.pwms.report.model.Report;
import com.pwms.report.model.Report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    // All reports for a patient — admin view (all statuses)
    List<Report> findByPatientId(int patientId);

    // Patient view — only PUBLISHED reports
    List<Report> findByPatientIdAndStatus(int patientId, ReportStatus status);

    // Date range — weekly/monthly analytics
    List<Report> findByPatientIdAndDateBetweenOrderByDateDesc(
            int patientId, LocalDate from, LocalDate to);

    // Published reports in date range
    List<Report> findByPatientIdAndStatusAndDateBetweenOrderByDateDesc(
            int patientId, ReportStatus status, LocalDate from, LocalDate to);



    // changed code
    List<Report> findAdminSummaryDetailsByPatientIdAndPlanId(int patientId,int planId);

}