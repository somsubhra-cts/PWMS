package com.pwms.progress.controller;

import com.pwms.progress.dto.*;
import com.pwms.progress.exception.*;
import com.pwms.progress.interfaces.ProgressIntf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "Progress",
        description = "Track daily activity completion and wellness progress")
public class ProgressController {

    private final ProgressIntf progressService;

    @Operation(summary = "Initialize progress for a patient",
            description = "Seeds one PENDING row per activity when a plan is assigned")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Progress initialized"),
            @ApiResponse(responseCode = "409", description = "Progress already initialized")
    })
    @PostMapping("/init")
    public ResponseEntity<String> initializeProgress(
            @Parameter(description = "Patient ID", required = true)
            @RequestParam int patientId,
            @Parameter(description = "Plan ID", required = true)
            @RequestParam int planId)
            throws ProgressAlreadyExistsException {
        progressService.initializeProgress(patientId, planId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Progress initialized for patientId: " + patientId +
                        " planId: " + planId);
    }

    @Operation(summary = "Update activity status",
            description = "Patient marks an activity as DONE, PENDING or SKIPPED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Progress row not found")
    })
    @PatchMapping("/update/{patientId}")
    public ResponseEntity<ProgressResponseDTO> updateStatus(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Valid @RequestBody StatusUpdateDTO dto)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(progressService.updateStatus(patientId, dto));
    }

    @Operation(summary = "Seed today's progress for a patient",
            description = "Creates a fresh set of PENDING rows for today's date using the same activities under the given plan. If already seeded for today, returns the existing rows.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress seeded or already exists for today")
    })
    @PostMapping("/seed")
    public ResponseEntity<List<ProgressResponseDTO>> seedProgress(
            @Parameter(description = "Patient ID", required = true)
            @RequestParam int patientId,
            @Parameter(description = "Plan ID", required = true)
            @RequestParam int planId) {
        List<ProgressResponseDTO> seeded = progressService.seedProgressForDate(patientId, planId);
        return ResponseEntity.ok(seeded);
    }

    @Operation(summary = "Get all progress for a patient",
            description = "Returns progress list with activity names and statuses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress found"),
            @ApiResponse(responseCode = "404", description = "No progress found")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ProgressResponseDTO>> getByPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(
                progressService.getProgressByPatient(patientId));
    }

    @Operation(summary = "Get progress for a patient and plan",
            description = "Returns progress filtered by both patient and plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress found"),
            @ApiResponse(responseCode = "404", description = "No progress found")
    })
    @GetMapping("/patient/{patientId}/plan/{planId}")
    public ResponseEntity<List<ProgressResponseDTO>> getByPatientAndPlan(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Plan ID", required = true)
            @PathVariable int planId)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(
                progressService.getProgressByPatientAndPlan(patientId, planId));
    }

    @Operation(summary = "Get daily completion summary",
            description = "Returns total activities, completed count and completion percentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary calculated"),
            @ApiResponse(responseCode = "404", description = "No activities found for plan")
    })
    @GetMapping("/summary/{patientId}/plan/{planId}")
    public ResponseEntity<ProgressSummaryDTO> getDailySummary(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Plan ID", required = true)
            @PathVariable int planId)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(
                progressService.getDailySummary(patientId, planId));
    }

    @Operation(summary = "Get progress for a patient on a specific date",
            description = "Returns all activity progress for a patient on the given date across all plans")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress found"),
            @ApiResponse(responseCode = "404", description = "No progress found for that date")
    })
    @GetMapping("/patient/{patientId}/date/{date}")
    public ResponseEntity<List<ProgressResponseDTO>> getByPatientAndDate(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Date in YYYY-MM-DD format", required = true)
            @PathVariable LocalDate date)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(
                progressService.getProgressByPatientAndDate(patientId, date));
    }

    @Operation(summary = "Get progress for a patient, plan and specific date",
            description = "Returns activity progress filtered by patient, plan and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress found"),
            @ApiResponse(responseCode = "404", description = "No progress found for that date")
    })
    @GetMapping("/patient/{patientId}/plan/{planId}/date/{date}")
    public ResponseEntity<List<ProgressResponseDTO>> getByPatientAndPlanAndDate(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int patientId,
            @Parameter(description = "Plan ID", required = true)
            @PathVariable int planId,
            @Parameter(description = "Date in YYYY-MM-DD format", required = true)
            @PathVariable LocalDate date)
            throws ProgressNotFoundException {
        return ResponseEntity.ok(
                progressService.getProgressByPatientAndPlanAndDate(patientId, planId, date));
    }
}
