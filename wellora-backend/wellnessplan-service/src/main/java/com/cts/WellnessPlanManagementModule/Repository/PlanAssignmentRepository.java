package com.cts.WellnessPlanManagementModule.Repository;

import com.cts.WellnessPlanManagementModule.Model.PlanAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanAssignmentRepository extends JpaRepository<PlanAssignment, Long> {

    // All active plans for a patient
    List<PlanAssignment> findByPatientIdAndStatus(Long patientId, PlanAssignment.AssignmentStatus status);

    // All patients on a given plan
    List<PlanAssignment> findByWellnessPlan_PlanIdAndStatus(Long planId, PlanAssignment.AssignmentStatus status);

    // Check if patient is already assigned to this plan
    Optional<PlanAssignment> findByPatientIdAndWellnessPlan_PlanId(Long patientId, Long planId);
}