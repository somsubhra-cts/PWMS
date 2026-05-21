package com.cts.WellnessPlanManagementModule.Controller;


import com.cts.WellnessPlanManagementModule.DTO.RequestDTO.CreateWellnessPlanDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.*;
import com.cts.WellnessPlanManagementModule.Exception.PatientNotFoundException;
import com.cts.WellnessPlanManagementModule.Exception.PatientPlanNotExistsException;

import com.cts.WellnessPlanManagementModule.Service.WellnessPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Wellness Plan Endpoint",
        description = "Endpoints that manages all the activities of wellness plan"
)
@RestController
@RequestMapping("/api/plans")
public class WellnessPlanController {

    @Autowired
    private WellnessPlanService wellnessPlanService;

    // ── CREATE WELLNESS PLAN ───────────────────────────────────────────────────

    @Operation(
            summary = "Create a wellness plan",
            description = "Creates a new wellness plan with the provided name and activities"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Wellness plan created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WellnessPlanResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan name is required\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<WellnessPlanResponseDTO> createWellnessPlan(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Wellness plan details to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateWellnessPlanDTO.class))
            )
            @RequestBody CreateWellnessPlanDTO wellnessPlan)
    {


        WellnessPlanResponseDTO responseDTO = wellnessPlanService.addWellnessPlan(wellnessPlan);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // ── GET PLAN BY ID ─────────────────────────────────────────────────────────

    @Operation(
            summary = "Get wellness plan by ID",
            description = "Fetches a single wellness plan along with its activities based on plan ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WellnessPlanResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan not found with given ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan not exists with a Given ID\"}")
                    )
            )
    })
    @GetMapping("/{planId}")
    public ResponseEntity<WellnessPlanResponseDTO> getPlanDetailsBasedOnId(
            @Parameter(description = "ID of the wellness plan to fetch", required = true, example = "1")
            @PathVariable("planId") Long id) throws Exception {
        WellnessPlanResponseDTO planOnId = wellnessPlanService.getPlanInfo(id);
        return ResponseEntity.ok(planOnId);
    }

    // ── GET ALL PLANS ──────────────────────────────────────────────────────────

    @Operation(
            summary = "Get all wellness plans",
            description = "Returns a list of all wellness plans with their activities"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of plans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WellnessPlanResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No plans found",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<WellnessPlanResponseDTO>> getAllPlans() throws Exception {
        List<WellnessPlanResponseDTO> datas = wellnessPlanService.fetchAllPlans();
        return ResponseEntity.ok(datas);
    }

    // ── UPDATE PLAN ────────────────────────────────────────────────────────────

    @Operation(
            summary = "Update a wellness plan",
            description = "Updates the details of an existing wellness plan by plan ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WellnessPlanResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan not found with given ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan not exists with a Given ID\"}")
                    )
            )
    })
    @PutMapping("/{planId}")
    public ResponseEntity<WellnessPlanResponseDTO> updatePlanDetails(
            @Parameter(description = "ID of the plan to update", required = true, example = "1")
            @PathVariable Long planId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated wellness plan details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateWellnessPlanDTO.class))
            )
            @RequestBody CreateWellnessPlanDTO newPlan) throws Exception {
        WellnessPlanResponseDTO dto = wellnessPlanService.updatePlan(planId, newPlan);
        return ResponseEntity.ok(dto);
    }

    // ── DELETE PLAN ────────────────────────────────────────────────────────────

    @Operation(
            summary = "Delete a wellness plan",
            description = "Deletes a wellness plan and its associated activities by plan ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "WellnessPlan with id 1 was deleted Successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan not found with given ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan not exists with a Given ID\"}")
                    )
            )
    })
    @DeleteMapping("/{planId}")
    public ResponseEntity<String> deleteWellnessPlan(
            @Parameter(description = "ID of the plan to delete", required = true, example = "1")
            @PathVariable Long planId) throws PatientPlanNotExistsException {
        wellnessPlanService.deleteWellnessPlan(planId);
        return ResponseEntity.ok("WellnessPlan with id " + planId + " was deleted Successfully");
    }

    // ── ASSIGN PLAN TO PATIENT ─────────────────────────────────────────────────

    @Operation(
            summary = "Assign a wellness plan to a patient",
            description = "Assigns an existing wellness plan to a patient using their respective IDs"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan assigned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "Patient with id 1 was assigned with Plan id 2 successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan or Patient not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan not exists with a Given ID\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Patient already assigned to this plan",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Patient already assigned to this plan\"}")
                    )
            )
    })
    @PostMapping("/assign")
    public ResponseEntity<String> assignPlanToPatients(
            @Parameter(description = "ID of the patient", required = true, example = "1")
            @RequestParam("patientId") Long patientId,
            @Parameter(description = "ID of the wellness plan", required = true, example = "1")
            @RequestParam("planId") Long planId) throws PatientPlanNotExistsException, PatientNotFoundException {
        wellnessPlanService.assignPlanToPatient(patientId, planId);
        return ResponseEntity.ok("Patient with id " + patientId + " was assigned with Plan id " + planId + " successfully");
    }

    // ── GET ACTIVITIES BY PLAN ID ──────────────────────────────────────────────

    @Operation(
            summary = "Get activities by plan ID",
            description = "Returns all activities associated with a specific wellness plan"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Activities returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ActivitiesDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan not found with given ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Plan not exists with a Given ID\"}")
                    )
            )
    })
    @GetMapping("/{planId}/activities")
    public ResponseEntity<List<ActivitiesDTO>> getActivitiesInfoByPlanId(
            @Parameter(description = "ID of the wellness plan", required = true, example = "1")
            @PathVariable("planId") Long planId) throws PatientPlanNotExistsException {
        return ResponseEntity.ok(wellnessPlanService.getPlanActivities(planId));
    }

    // ── GET ASSIGNMENTS BY PATIENT ─────────────────────────────────────────────

    @Operation(
            summary = "Get active assignments by patient",
            description = "Returns all active wellness plan assignments for a given patient"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignments returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlanAssignmentDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No assignments found for given patient",
                    content = @Content
            )
    })
    @GetMapping("/assignments/patient/{patientId}")
    public ResponseEntity<List<PlanAssignmentDTO>> getByPatient(
            @Parameter(description = "ID of the patient", required = true, example = "1")
            @PathVariable Long patientId) {
        return ResponseEntity.ok(wellnessPlanService.getActiveAssignmentsByPatient(patientId));
    }

    // ── GET ASSIGNMENTS BY PLAN ────────────────────────────────────────────────

    @Operation(
            summary = "Get active assignments by plan",
            description = "Returns all patients assigned to a given wellness plan"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignments found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlanAssignmentDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No assignments found for given plan",
                    content = @Content
            )
    })
    @GetMapping("/assignments/plan/{planId}")
    public ResponseEntity<List<PlanAssignmentDTO>> getByPlan(
            @Parameter(description = "ID of the wellness plan", required = true, example = "1")
            @PathVariable Long planId) {
        return ResponseEntity.ok(wellnessPlanService.getActiveAssignmentsByPlan(planId));
    }
}




