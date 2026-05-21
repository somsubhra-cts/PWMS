package com.pwms.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwms.report.dto.*;
import com.pwms.report.exception.ReportNotFoundException;
import com.pwms.report.interfaces.ReportIntf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false",
        "spring.cloud.config.enabled=false"
})
class ReportControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean
    private ReportIntf reportService;

    private ReportDTO        reportDTO;
    private ReportPreviewDTO previewDTO;
    private ReportRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        reportDTO = ReportDTO.builder()
                .reportId(1)
                .patientId(1)
                .patientName("Ashok")
                .planId(1)
                .planName("Weight Loss")
                .generatedBy(1)
                .summary("Patient: Ashok | Completed: 2/3")
                .adminSummary("Good progress overall.")
                .date(LocalDate.now())
                .status("PUBLISHED")
                .totalActivities(3)
                .completedActivities(2)
                .completionPercentage(66.67)
                .build();

        previewDTO = ReportPreviewDTO.builder()
                .patientId(1)
                .patientName("Ashok")
                .planId(1)
                .planName("Weight Loss")
                .totalActivities(3)
                .completedActivities(2)
                .completionPercentage(66.67)
                .dateRange("2026-03-28 to 2026-04-03")
                .build();

        requestDTO = new ReportRequestDTO(1, "Good progress overall.");
    }

    // ── GET /api/reports/preview/{patientId}/{planId} ─────────

    @Test
    void getPreview_returns200() throws Exception {
        when(reportService.getReportPreview(1, 1))
                .thenReturn(previewDTO);

        mockMvc.perform(get("/api/reports/preview/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Ashok"))
                .andExpect(jsonPath("$.completionPercentage").value(66.67));
    }

    // ── POST /api/reports/generate/{patientId}/{planId} ───────

    @Test
    void generateReport_returns201() throws Exception {
        when(reportService.generateReport(
                eq(1), eq(1), any(ReportRequestDTO.class)))
                .thenReturn(reportDTO);

        mockMvc.perform(post("/api/reports/generate/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.patientName").value("Ashok"))
                .andExpect(jsonPath("$.adminSummary")
                        .value("Good progress overall."));
    }

    // ── GET /api/reports/patient/{patientId} ──────────────────

    @Test
    void getPublishedReports_returns200() throws Exception {
        when(reportService.getPublishedReportsByPatient(1))
                .thenReturn(List.of(reportDTO));

        mockMvc.perform(get("/api/reports/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPublishedReports_notFound_returns404() throws Exception {
        when(reportService.getPublishedReportsByPatient(99))
                .thenThrow(new ReportNotFoundException(
                        "No published reports found for patientId: 99"));

        mockMvc.perform(get("/api/reports/patient/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/reports/{reportId} ───────────────────────────

    @Test
    void getById_returns200() throws Exception {
        when(reportService.getReportById(1)).thenReturn(reportDTO);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(1))
                .andExpect(jsonPath("$.planName").value("Weight Loss"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(reportService.getReportById(99))
                .thenThrow(new ReportNotFoundException(
                        "Report not found with id: 99"));

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/reports/download/{reportId} ──────────────────

    @Test
    void downloadPdf_returns200() throws Exception {
        when(reportService.downloadReportPdf(1))
                .thenReturn("PDF".getBytes());

        mockMvc.perform(get("/api/reports/download/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=report_1.pdf"));
    }
}