package com.pwms.report.controller;

import com.pwms.report.dto.*;
import com.pwms.report.exception.ReportNotFoundException;
import com.pwms.report.interfaces.ReportIntf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report",
        description = "Generate wellness reports, analytics and PDF downloads")
public class ReportController {

    private final ReportIntf reportService;

    @Operation(summary = "Preview report before generating",
            description = "Returns pre-filled patient + progress data for admin to review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preview loaded"),
            @ApiResponse(responseCode = "404", description = "Patient or plan not found")
    })
    @GetMapping("/preview/{patientId}/{planId}")
    public ResponseEntity<ReportPreviewDTO> getPreview(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Plan ID", required = true)
            @PathVariable int planId)
            throws ReportNotFoundException {
        return ResponseEntity.ok(
                reportService.getReportPreview(patientId, planId));
    }

    @Operation(summary = "Generate and publish a report",
            description = "Admin submits overall summary — report saved and patient notified")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report generated and published"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Patient or plan not found")
    })
    @PostMapping("/generate/{patientId}/{planId}")
    public ResponseEntity<ReportDTO> generateReport(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Plan ID", required = true)
            @PathVariable int planId,
            @Valid @RequestBody ReportRequestDTO request)
            throws ReportNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.generateReport(patientId, planId, request));
    }

    @Operation(summary = "Admin — get all reports for a patient",
            description = "Returns all reports regardless of status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reports found"),
            @ApiResponse(responseCode = "404", description = "No reports found")
    })
    @GetMapping("/admin/patient/{patientId}")
    public ResponseEntity<List<ReportDTO>> getAllReports(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId)
            throws ReportNotFoundException {
        return ResponseEntity.ok(
                reportService.getAllReportsByPatient(patientId));
    }

    @Operation(summary = "Patient — get published reports",
            description = "Returns only PUBLISHED reports visible to the patient")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published reports found"),
            @ApiResponse(responseCode = "404", description = "No published reports found")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ReportDTO>> getPublishedReports(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId)
            throws ReportNotFoundException {
        return ResponseEntity.ok(
                reportService.getPublishedReportsByPatient(patientId));
    }

    @Operation(summary = "Get single report by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report found"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDTO> getById(
            @Parameter(description = "Report ID", required = true)
            @PathVariable int reportId)
            throws ReportNotFoundException {
        return ResponseEntity.ok(reportService.getReportById(reportId));
    }

    @Operation(summary = "Get reports by date range",
            description = "Filter reports between from and to dates — format: yyyy-MM-dd")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reports found"),
            @ApiResponse(responseCode = "404", description = "No reports in range")
    })
    @GetMapping("/patient/{patientId}/range")
    public ResponseEntity<List<ReportDTO>> getByDateRange(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "From date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "To date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
            throws ReportNotFoundException {
        return ResponseEntity.ok(
                reportService.getReportsByDateRange(patientId, from, to));
    }

    @Operation(summary = "Download report as PDF",
            description = "Returns PDF file for the given report ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF downloaded"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/download/{reportId}")
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "Report ID", required = true)
            @PathVariable int reportId)
            throws ReportNotFoundException {
        byte[] pdf = reportService.downloadReportPdf(reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report_" + reportId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}