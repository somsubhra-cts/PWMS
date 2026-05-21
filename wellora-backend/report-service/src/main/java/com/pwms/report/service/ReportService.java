package com.pwms.report.service;

import com.pwms.report.client.*;
import com.pwms.report.dto.*;
import com.pwms.report.exception.ReportNotFoundException;
import com.pwms.report.interfaces.ReportIntf;
import com.pwms.report.model.Report;
import com.pwms.report.model.Report.ReportStatus;
import com.pwms.report.repository.ReportRepository;
import com.pwms.report.util.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService implements ReportIntf {

    private final ReportRepository   reportRepo;
    private final PatientClient      patientClient;
    private final WellnessPlanClient planClient;
    private final ProgressClient     progressClient;
    private final NotificationClient notificationClient;
    private final PdfReportGenerator pdfGenerator;

    @Override
    public ReportPreviewDTO getReportPreview(int patientId, int planId)
            throws ReportNotFoundException {
        log.debug("Loading report preview — patientId: {} planId: {}",
                patientId, planId);
        try
        {
            PatientDTO         patient = patientClient.getPatientById(patientId);
            PlanDTO            plan    = planClient.getPlanById(planId);

        log.info("PLLLLLLLLLLLLL {}",plan.getPlanName());
            ProgressSummaryDTO summary =
                    progressClient.getDailySummary(patientId, planId);

            LocalDate from = LocalDate.now().minusDays(6);
            LocalDate to   = LocalDate.now();

            log.debug("Preview loaded — patient: {} plan: {} completion: {}%",
                    patient.getPatientName(), plan.getPlanName(),
                    summary.getCompletionPercentage());




            return ReportPreviewDTO.builder()
                    .patientId(patientId)
                    .patientName(patient.getPatientName())
                    .planId(planId)
                    .planName(plan.getPlanName())
                    .totalActivities(summary.getTotalActivities())
                    .completedActivities(summary.getCompletedActivities())
                    .completionPercentage(summary.getCompletionPercentage())
                    .dateRange(from + " to " + to)
                    .build();
        } catch (Exception e) {
            log.error("Failed to load preview — patientId: {} planId: {} — {}",
                    patientId, planId, e.getMessage());
            throw new ReportNotFoundException(
                    "Could not load preview: " + e.getMessage());
        }
    }

    @Override
    public ReportDTO generateReport(int patientId, int planId,
                                    ReportRequestDTO request)
            throws ReportNotFoundException {
        log.debug("Generating report — patientId: {} planId: {} adminId: {}",
                patientId, planId, request.getAdminId());
        try {
            PatientDTO         patient = patientClient.getPatientById(patientId);
            PlanDTO            plan    = planClient.getPlanById(planId);
            ProgressSummaryDTO summary =
                    progressClient.getDailySummary(patientId, planId);

            String systemSummary = String.format(
                    "Patient: %s | Plan: %s | Date: %s | Completed: %d/%d (%.2f%%)",
                    patient.getPatientName(), plan.getPlanName(),
                    LocalDate.now(),
                    summary.getCompletedActivities(),
                    summary.getTotalActivities(),
                    summary.getCompletionPercentage());

            Report saved = reportRepo.save(Report.builder()
                    .patientId(patientId).planId(planId)
                    .generatedBy(request.getAdminId())
                    .summary(systemSummary)
                    .adminSummary(request.getAdminSummary())
                    .status(ReportStatus.PUBLISHED)
                    .build());

            log.info("Report generated — id: {} patientId: {} planId: {}",
                    saved.getReportId(), patientId, planId);

            try {
                notificationClient.notifyReportShared(patientId, planId);
                log.debug("Patient {} notified — report shared", patientId);
            } catch (Exception e) {
                log.error("Failed to notify report shared — patientId: {}",
                        patientId);
            }


            // modified code

            boolean planCompleted = planClient
                    .getActiveAssignmentsByPatient(patientId)
                    .stream()
                    .noneMatch(a -> a.getPlanId() == planId
                            && "ACTIVE".equals(a.getStatus()));



            log.error("Plan completed Status :  {} ",planCompleted);

            if (planCompleted) {
                try {
                    notificationClient.notifyAppointmentReminder(
                            patientId, planId);
                    log.info("Appointment reminder sent — patientId: {}",
                            patientId);
                } catch (Exception e) {
                    log.error("Failed to send appointment reminder — {}",
                            e.getMessage());
                }
            }

            return toDTO(saved, patient.getPatientName(),
                    plan.getPlanName(), summary);

        } catch (Exception e) {
            log.error("Report generation failed — patientId: {} planId: {} — {}",
                    patientId, planId, e.getMessage());
            throw new ReportNotFoundException(
                    "Failed to generate report: " + e.getMessage());
        }
    }

    @Override
    public List<ReportDTO> getAllReportsByPatient(int patientId)
            throws ReportNotFoundException {
        log.debug("Fetching all reports for patientId: {}", patientId);
        List<Report> reports = reportRepo.findByPatientId(patientId);
        if (reports.isEmpty()) {
            log.warn("No reports found for patientId: {}", patientId);
            throw new ReportNotFoundException(
                    "No reports found for patientId: " + patientId);
        }
        return enrichList(reports);
    }

    @Override
    public List<ReportDTO> getPublishedReportsByPatient(int patientId)
            throws ReportNotFoundException {
        log.debug("Fetching published reports for patientId: {}", patientId);
        List<Report> reports = reportRepo.findByPatientIdAndStatus(
                patientId, ReportStatus.PUBLISHED);
        if (reports.isEmpty()) {
            log.warn("No published reports for patientId: {}", patientId);
            throw new ReportNotFoundException(
                    "No published reports found for patientId: " + patientId);
        }
        return enrichList(reports);
    }

    @Override
    public ReportDTO getReportById(int reportId)
            throws ReportNotFoundException {
        log.debug("Fetching report id: {}", reportId);
        Report r = reportRepo.findById(reportId)
                .orElseThrow(() -> {
                    log.warn("Report not found: {}", reportId);
                    return new ReportNotFoundException(
                            "Report not found with id: " + reportId);
                });
        return enrichSingle(r);
    }

    @Override
    public List<ReportDTO> getReportsByDateRange(
            int patientId, LocalDate from, LocalDate to)
            throws ReportNotFoundException {
        log.debug("Fetching reports for patientId: {} from: {} to: {}",
                patientId, from, to);
        List<Report> reports = reportRepo
                .findByPatientIdAndDateBetweenOrderByDateDesc(patientId, from, to);
        if (reports.isEmpty()) {
            log.warn("No reports between {} and {} for patientId: {}",
                    from, to, patientId);
            throw new ReportNotFoundException(
                    "No reports found between " + from + " and " + to);
        }
        return enrichList(reports);
    }

    @Override
    public byte[] downloadReportPdf(int reportId)
            throws ReportNotFoundException {
        log.debug("Generating PDF for reportId: {}", reportId);
        byte[] pdf = pdfGenerator.generate(getReportById(reportId));
        log.info("PDF generated for reportId: {} size: {} bytes",
                reportId, pdf.length);
        return pdf;
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<ReportDTO> enrichList(List<Report> reports) {
        return reports.stream()
                .map(this::enrichSingle)
                .collect(Collectors.toList());
    }

    private ReportDTO enrichSingle(Report r) {
        String patientName = "Unknown";
        String planName    = "Unknown";
        int total = 0, completed = 0;
        double pct = 0.0;
        try {
            patientName =
                    patientClient.getPatientById(r.getPatientId()).getPatientName();
            planName =
                    planClient.getPlanById(r.getPlanId()).getPlanName();
            ProgressSummaryDTO s =
                    progressClient.getDailySummary(r.getPatientId(), r.getPlanId());
            total     = s.getTotalActivities();
            completed = s.getCompletedActivities();
            pct       = s.getCompletionPercentage();
        } catch (Exception e) {
            log.error("Failed to enrich report {} — {}",
                    r.getReportId(), e.getMessage());
        }
        return toDTO(r, patientName, planName, total, completed, pct);
    }

    private ReportDTO toDTO(Report r, String patientName, String planName,
                            ProgressSummaryDTO s) {
        return toDTO(r, patientName, planName,
                s.getTotalActivities(),
                s.getCompletedActivities(),
                s.getCompletionPercentage());
    }

    private ReportDTO toDTO(Report r, String patientName, String planName,
                            int total, int completed, double pct) {
        return ReportDTO.builder()
                .reportId(r.getReportId())
                .patientId(r.getPatientId())
                .patientName(patientName)
                .planId(r.getPlanId())
                .planName(planName)
                .generatedBy(r.getGeneratedBy())
                .summary(r.getSummary())
                .adminSummary(r.getAdminSummary())
                .date(r.getDate())
                .status(r.getStatus().name())
                .totalActivities(total)
                .completedActivities(completed)
                .completionPercentage(pct)
                .build();
    }
}