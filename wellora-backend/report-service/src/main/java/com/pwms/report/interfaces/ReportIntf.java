package com.pwms.report.interfaces;

import com.pwms.report.dto.*;
import com.pwms.report.exception.ReportNotFoundException;

import java.time.LocalDate;
import java.util.List;

public interface ReportIntf {

    // Step 1 — Admin previews pre-filled data before writing summary
    ReportPreviewDTO getReportPreview(int patientId, int planId)
            throws ReportNotFoundException;

    // Step 2 — Admin submits report with their summary → saved + patient notified
    ReportDTO generateReport(int patientId, int planId, ReportRequestDTO request)
            throws ReportNotFoundException;

    // Admin view — all reports regardless of status
    List<ReportDTO> getAllReportsByPatient(int patientId)
            throws ReportNotFoundException;

    // Patient view — only PUBLISHED reports
    List<ReportDTO> getPublishedReportsByPatient(int patientId)
            throws ReportNotFoundException;

    // Single report by ID
    ReportDTO getReportById(int reportId)
            throws ReportNotFoundException;

    // Date range analytics — admin use
    List<ReportDTO> getReportsByDateRange(int patientId, LocalDate from, LocalDate to)
            throws ReportNotFoundException;

    // PDF download using saved report data
    byte[] downloadReportPdf(int reportId)
            throws ReportNotFoundException;
}