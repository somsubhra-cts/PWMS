package com.pwms.report.unit;

import com.pwms.report.client.*;
import com.pwms.report.dto.*;
import com.pwms.report.exception.ReportNotFoundException;
import com.pwms.report.model.Report;
import com.pwms.report.model.Report.ReportStatus;
import com.pwms.report.repository.ReportRepository;
import com.pwms.report.service.ReportService;
import com.pwms.report.util.PdfReportGenerator;
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
class ReportServiceTest {

    @Mock private ReportRepository   reportRepo;
    @Mock private PatientClient      patientClient;
    @Mock private WellnessPlanClient planClient;
    @Mock private ProgressClient     progressClient;
    @Mock private NotificationClient notificationClient;
    @Mock private PdfReportGenerator pdfGenerator;

    @InjectMocks
    private ReportService reportService;

    private Report             report;
    private PatientDTO         patientDTO;
    private PlanDTO            planDTO;
    private ProgressSummaryDTO summaryDTO;
    private ReportRequestDTO   requestDTO;

    @BeforeEach
    void setUp() {
        report = new Report();
        report.setReportId(1);
        report.setPatientId(1);
        report.setPlanId(1);
        report.setGeneratedBy(1);
        report.setSummary("Patient: Ashok | Plan: Weight Loss | Completed: 2/3");
        report.setAdminSummary("Good progress overall.");
        report.setDate(LocalDate.now());
        report.setStatus(ReportStatus.PUBLISHED);

        patientDTO = new PatientDTO(1, "Ashok", 21,
                "ashok@gmail.com", "None");

        planDTO = new PlanDTO(1, "Weight Loss");

        summaryDTO = ProgressSummaryDTO.builder()
                .patientId(1).planId(1)
                .date(LocalDate.now())
                .totalActivities(3)
                .completedActivities(2)
                .completionPercentage(66.67)
                .build();

        requestDTO = new ReportRequestDTO(1, "Good progress overall.");
    }

    // ── getReportPreview ──────────────────────────────────────

    @Test
    void getReportPreview_success() throws ReportNotFoundException {
        when(patientClient.getPatientById(1)).thenReturn(patientDTO);
        when(planClient.getPlanById(1)).thenReturn(planDTO);
        when(progressClient.getDailySummary(1, 1)).thenReturn(summaryDTO);

        ReportPreviewDTO result = reportService.getReportPreview(1, 1);

        assertNotNull(result);
        assertEquals("Ashok", result.getPatientName());
        assertEquals("Weight Loss", result.getPlanName());
        assertEquals(3, result.getTotalActivities());
        assertEquals(66.67, result.getCompletionPercentage());
    }

    // ── generateReport ────────────────────────────────────────

    @Test
    void generateReport_success() throws ReportNotFoundException {
        when(patientClient.getPatientById(1)).thenReturn(patientDTO);
        when(planClient.getPlanById(1)).thenReturn(planDTO);
        when(progressClient.getDailySummary(1, 1)).thenReturn(summaryDTO);
        when(reportRepo.save(any(Report.class))).thenReturn(report);
        when(planClient.getActiveAssignmentsByPatient(1))
                .thenReturn(List.of());
        doNothing().when(notificationClient)
                .notifyReportShared(anyInt(), anyInt());

        ReportDTO result = reportService.generateReport(1, 1, requestDTO);

        assertNotNull(result);
        assertEquals("Ashok", result.getPatientName());
        assertEquals("PUBLISHED", result.getStatus());
        verify(notificationClient, times(1))
                .notifyReportShared(1, 1);
    }

    // ── getReportById ─────────────────────────────────────────

    @Test
    void getReportById_success() throws ReportNotFoundException {
        when(reportRepo.findById(1)).thenReturn(Optional.of(report));
        when(patientClient.getPatientById(1)).thenReturn(patientDTO);
        when(planClient.getPlanById(1)).thenReturn(planDTO);
        when(progressClient.getDailySummary(1, 1)).thenReturn(summaryDTO);

        ReportDTO result = reportService.getReportById(1);

        assertEquals(1, result.getReportId());
        assertEquals("Ashok", result.getPatientName());
    }

    @Test
    void getReportById_notFound_throwsException() {
        when(reportRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(ReportNotFoundException.class,
                () -> reportService.getReportById(99));
    }

    // ── getPublishedReportsByPatient ──────────────────────────

    @Test
    void getPublishedReports_success() throws ReportNotFoundException {
        when(reportRepo.findByPatientIdAndStatus(1, ReportStatus.PUBLISHED))
                .thenReturn(List.of(report));
        when(patientClient.getPatientById(1)).thenReturn(patientDTO);
        when(planClient.getPlanById(1)).thenReturn(planDTO);
        when(progressClient.getDailySummary(1, 1)).thenReturn(summaryDTO);

        List<ReportDTO> result =
                reportService.getPublishedReportsByPatient(1);

        assertEquals(1, result.size());
        assertEquals("PUBLISHED", result.get(0).getStatus());
    }

    @Test
    void getPublishedReports_notFound_throwsException() {
        when(reportRepo.findByPatientIdAndStatus(99, ReportStatus.PUBLISHED))
                .thenReturn(List.of());

        assertThrows(ReportNotFoundException.class,
                () -> reportService.getPublishedReportsByPatient(99));
    }

    // ── downloadReportPdf ─────────────────────────────────────

    @Test
    void downloadReportPdf_success() throws ReportNotFoundException {
        when(reportRepo.findById(1)).thenReturn(Optional.of(report));
        when(patientClient.getPatientById(1)).thenReturn(patientDTO);
        when(planClient.getPlanById(1)).thenReturn(planDTO);
        when(progressClient.getDailySummary(1, 1)).thenReturn(summaryDTO);
        when(pdfGenerator.generate(any(ReportDTO.class)))
                .thenReturn("PDF".getBytes());

        byte[] result = reportService.downloadReportPdf(1);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(pdfGenerator, times(1)).generate(any(ReportDTO.class));
    }
}