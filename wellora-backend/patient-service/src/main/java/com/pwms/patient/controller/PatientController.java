package com.pwms.patient.controller;

import com.pwms.patient.exception.PatientAlreadyExistsException;
import com.pwms.patient.exception.PatientNotFoundException;
import com.pwms.patient.interfaces.PatientIntf;
import com.pwms.patient.model.Patient;
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

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patient", description = "Patient profile management endpoints")
public class PatientController {

    private final PatientIntf patientService;

    @Operation(summary = "Add a new patient",
            description = "Creates a new patient profile and notifies admin")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patient created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })





    // add patitent to the database
    @PostMapping
    public ResponseEntity<Patient> addPatient(
            @Valid @RequestBody Patient patient)
            throws PatientAlreadyExistsException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.addPatient(patient));
    }

    @Operation(summary = "Get all patients")
    @ApiResponse(responseCode = "200", description = "List of all patients")
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.findAllPatients());
    }

    @Operation(summary = "Get patient by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int id)
            throws PatientNotFoundException {
        return ResponseEntity.ok(patientService.findPatientById(id));
    }

    @Operation(summary = "Search patient by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/search")
    public ResponseEntity<Patient> getPatientByName(
            @Parameter(description = "Patient name to search")
            @RequestParam String name)
            throws PatientNotFoundException {
        return ResponseEntity.ok(patientService.findPatientByName(name));
    }

    @Operation(summary = "Update patient by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int id,
            @Valid @RequestBody Patient updatedPatient)
            throws PatientNotFoundException {
        return ResponseEntity.ok(
                patientService.updatePatientById(id, updatedPatient));
    }

    @Operation(summary = "Delete patient by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient deleted"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable int id)
            throws PatientNotFoundException {
        patientService.deletePatientById(id);
        return ResponseEntity.ok(
                "Patient with id " + id + " deleted successfully.");
    }
}

