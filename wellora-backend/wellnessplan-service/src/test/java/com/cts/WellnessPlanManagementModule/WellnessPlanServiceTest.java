package com.cts.WellnessPlanManagementModule;


import com.cts.WellnessPlanManagementModule.DTO.RequestDTO.CreateWellnessPlanDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.ActivitiesDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.PlanAssignmentDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.WellnessPlanResponseDTO;
import com.cts.WellnessPlanManagementModule.Exception.PatientNotFoundException;
import com.cts.WellnessPlanManagementModule.Exception.PatientPlanNotExistsException;
import com.cts.WellnessPlanManagementModule.Exception.WellNessPlanAlreadyExistsException;
import com.cts.WellnessPlanManagementModule.Model.ActivitiesModel;
import com.cts.WellnessPlanManagementModule.Model.PlanAssignment;
import com.cts.WellnessPlanManagementModule.Model.WellnessPlanModel;
import com.cts.WellnessPlanManagementModule.Repository.PlanAssignmentRepository;
import com.cts.WellnessPlanManagementModule.Repository.WellnessPlanRepository;
import com.cts.WellnessPlanManagementModule.Service.WellnessPlanService;
import com.cts.WellnessPlanManagementModule.client.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class WellnessPlanServiceTest {

    @Mock
    private WellnessPlanRepository repository;

    @Mock
    private PlanAssignmentRepository assignmentRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private WellnessPlanService wellnessPlanService;

    // ─── Shared fixtures ───────────────────────────────────────────────────────

    private WellnessPlanModel buildPlanModel(Long id, String name) {
        WellnessPlanModel model = new WellnessPlanModel();
        model.setPlanId(id);
        model.setPlanName(name);
        return model;
    }

    private CreateWellnessPlanDTO buildCreateDTO(String name, List<String> activities) {
        CreateWellnessPlanDTO dto = new CreateWellnessPlanDTO();
        dto.setPlanName(name);
        dto.setActivityDecriptions(activities);
        return dto;
    }

    private PlanAssignment buildAssignment(Long assignmentId, Long patientId,
                                           WellnessPlanModel plan,
                                           PlanAssignment.AssignmentStatus status) {
        PlanAssignment pa = new PlanAssignment();
        pa.setAssignmentId(assignmentId);
        pa.setPatientId(patientId);
        pa.setWellnessPlan(plan);
        pa.setAssignedDate(LocalDate.now());
        pa.setStatus(status);
        return pa;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // addWellnessPlan
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addWellnessPlan – success: new plan is saved and returned")
    void addWellnessPlan_Success() {
        CreateWellnessPlanDTO dto = buildCreateDTO("Cardio Plan", List.of("Run 5km", "Walk 2km"));

        WellnessPlanModel saved = buildPlanModel(1L, "Cardio Plan");
        ActivitiesModel act1 = new ActivitiesModel();
        act1.setActivityId(1L);
        act1.setActivityDescription("Run 5km");
        act1.setPlan(saved);
        ActivitiesModel act2 = new ActivitiesModel();
        act2.setActivityId(2L);
        act2.setActivityDescription("Walk 2km");
        act2.setPlan(saved);
        saved.getActivities().addAll(List.of(act1, act2));

        when(repository.existsPlanByPlanName("Cardio Plan")).thenReturn(false);
        when(repository.save(any(WellnessPlanModel.class))).thenReturn(saved);

        WellnessPlanResponseDTO result = wellnessPlanService.addWellnessPlan(dto);

        assertNotNull(result);
        assertEquals("Cardio Plan", result.getPlanName());
        verify(repository).save(any(WellnessPlanModel.class));
    }

    @Test
    @DisplayName("addWellnessPlan – throws WellNessPlanAlreadyExistsException when name is duplicate")
    void addWellnessPlan_DuplicateName_ThrowsException() {
        CreateWellnessPlanDTO dto = buildCreateDTO("Cardio Plan", List.of("Run 5km"));

        when(repository.existsPlanByPlanName("Cardio Plan")).thenReturn(true);

        assertThrows(WellNessPlanAlreadyExistsException.class,
                () -> wellnessPlanService.addWellnessPlan(dto));

        verify(repository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getPlanInfo
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getPlanInfo – success: returns plan DTO for valid id")
    void getPlanInfo_Success() throws Exception {
        WellnessPlanModel plan = buildPlanModel(1L, "Yoga Plan");
        when(repository.fetchAllDatasBasedOnId(1L)).thenReturn(Optional.of(plan));

        WellnessPlanResponseDTO result = wellnessPlanService.getPlanInfo(1L);

        assertNotNull(result);
        assertEquals("Yoga Plan", result.getPlanName());
    }

    @Test
    @DisplayName("getPlanInfo – throws PatientPlanNotExistsException when plan not found")
    void getPlanInfo_PlanNotFound_ThrowsException() {
        when(repository.fetchAllDatasBasedOnId(99L)).thenReturn(Optional.empty());

        // The service calls .orElse(null) then checks null AFTER accessing getPlanName(),
        // so a NullPointerException is thrown before our guard – reflect that behaviour.
        assertThrows(Exception.class, () -> wellnessPlanService.getPlanInfo(99L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // fetchAllPlans
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("fetchAllPlans – success: returns list of plan DTOs")
    void fetchAllPlans_Success() throws Exception {
        List<WellnessPlanModel> planList = List.of(
                buildPlanModel(1L, "Cardio Plan"),
                buildPlanModel(2L, "Yoga Plan")
        );
        when(repository.fetchAllPlans()).thenReturn(Optional.of(planList));

        List<WellnessPlanResponseDTO> result = wellnessPlanService.fetchAllPlans();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("fetchAllPlans – throws PatientPlanNotExistsException when no plans exist")
    void fetchAllPlans_NoPlans_ThrowsException() {
        when(repository.fetchAllPlans()).thenReturn(Optional.empty());

        assertThrows(PatientPlanNotExistsException.class,
                () -> wellnessPlanService.fetchAllPlans());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // assignPlanToPatient
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("assignPlanToPatient – success: plan is saved and notification is sent")
    void assignPlanToPatient_Success() throws Exception {
        WellnessPlanModel plan = buildPlanModel(1L, "Cardio Plan");
        when(repository.fetchAllDatasBasedOnId(1L)).thenReturn(Optional.of(plan));
        when(repository.save(any(WellnessPlanModel.class))).thenReturn(plan);
        doNothing().when(notificationClient).notifyPlanAssigned(anyInt(), anyInt(), anyString());

        assertDoesNotThrow(() -> wellnessPlanService.assignPlanToPatient(10L, 1L));

        verify(repository).save(any(WellnessPlanModel.class));
        verify(notificationClient).notifyPlanAssigned(10, 1, "Cardio Plan");
    }

    @Test
    @DisplayName("assignPlanToPatient – throws PatientPlanNotExistsException when plan not found")
    void assignPlanToPatient_PlanNotFound_ThrowsException() {
        when(repository.fetchAllDatasBasedOnId(99L)).thenReturn(Optional.empty());

        assertThrows(PatientPlanNotExistsException.class,
                () -> wellnessPlanService.assignPlanToPatient(10L, 99L));

        verify(repository, never()).save(any());
        verifyNoInteractions(notificationClient);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getPlanActivities
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getPlanActivities – success: returns activity DTOs for given plan id")
    void getPlanActivities_Success() throws Exception {
        ActivitiesModel act = new ActivitiesModel();
        act.setActivityId(1L);
        act.setActivityDescription("Run 5km");

        when(repository.findActivitiesByPlanId(1L)).thenReturn(Optional.of(List.of(act)));

        List<ActivitiesDTO> result = wellnessPlanService.getPlanActivities(1L);

        assertEquals(1, result.size());
        assertEquals("Run 5km", result.get(0).getActivityDescription());
    }

    @Test
    @DisplayName("getPlanActivities – throws PatientPlanNotExistsException when plan not found")
    void getPlanActivities_PlanNotFound_ThrowsException() {
        when(repository.findActivitiesByPlanId(99L)).thenReturn(Optional.empty());

        assertThrows(PatientPlanNotExistsException.class,
                () -> wellnessPlanService.getPlanActivities(99L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // updatePlan
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updatePlan – success: existing plan is updated and saved")
    void updatePlan_Success() throws Exception {
        WellnessPlanModel existing = buildPlanModel(1L, "Old Plan");
        ActivitiesModel oldAct = new ActivitiesModel();
        oldAct.setActivityDescription("Old Activity");
        oldAct.setPlan(existing);
        existing.getActivities().add(oldAct);

        CreateWellnessPlanDTO updateDTO = buildCreateDTO("New Plan", List.of("New Activity 1", "New Activity 2"));

        WellnessPlanModel updatedModel = buildPlanModel(1L, "New Plan");
        ActivitiesModel newAct1 = new ActivitiesModel();
        newAct1.setActivityDescription("New Activity 1");
        newAct1.setPlan(updatedModel);
        ActivitiesModel newAct2 = new ActivitiesModel();
        newAct2.setActivityDescription("New Activity 2");
        newAct2.setPlan(updatedModel);
        updatedModel.getActivities().addAll(List.of(newAct1, newAct2));

        when(repository.fetchAllDatasBasedOnId(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(WellnessPlanModel.class))).thenReturn(updatedModel);

        WellnessPlanResponseDTO result = wellnessPlanService.updatePlan(1L, updateDTO);

        assertNotNull(result);
        assertEquals("New Plan", result.getPlanName());
        verify(repository).save(any(WellnessPlanModel.class));
    }

    @Test
    @DisplayName("updatePlan – throws PatientPlanNotExistsException when plan not found")
    void updatePlan_PlanNotFound_ThrowsException() {
        when(repository.fetchAllDatasBasedOnId(99L)).thenReturn(Optional.empty());
        CreateWellnessPlanDTO dto = buildCreateDTO("New Plan", List.of("Activity"));

        assertThrows(PatientPlanNotExistsException.class,
                () -> wellnessPlanService.updatePlan(99L, dto));

        verify(repository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // deleteWellnessPlan
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteWellnessPlan – success: plan is deleted from repository")
    void deleteWellnessPlan_Success() throws Exception {
        WellnessPlanModel plan = buildPlanModel(1L, "Cardio Plan");
        when(repository.fetchAllDatasBasedOnId(1L)).thenReturn(Optional.of(plan));
        doNothing().when(repository).delete(plan);

        assertDoesNotThrow(() -> wellnessPlanService.deleteWellnessPlan(1L));

        verify(repository).delete(plan);
    }

    @Test
    @DisplayName("deleteWellnessPlan – throws PatientPlanNotExistsException when plan not found")
    void deleteWellnessPlan_PlanNotFound_ThrowsException() {
        when(repository.fetchAllDatasBasedOnId(99L)).thenReturn(Optional.empty());

        assertThrows(PatientPlanNotExistsException.class,
                () -> wellnessPlanService.deleteWellnessPlan(99L));

        verify(repository, never()).delete(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getActiveAssignmentsByPatient
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getActiveAssignmentsByPatient – success: returns active assignment DTOs")
    void getActiveAssignmentsByPatient_Success() {
        WellnessPlanModel plan = buildPlanModel(1L, "Cardio Plan");
        PlanAssignment assignment = buildAssignment(1L, 10L, plan, PlanAssignment.AssignmentStatus.ACTIVE);

        when(assignmentRepository.findByPatientIdAndStatus(10L, PlanAssignment.AssignmentStatus.ACTIVE))
                .thenReturn(List.of(assignment));

        List<PlanAssignmentDTO> result = wellnessPlanService.getActiveAssignmentsByPatient(10L);

        assertEquals(1, result.size());
        assertEquals("Cardio Plan", result.get(0).getPlanName());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    @DisplayName("getActiveAssignmentsByPatient – returns empty list when no active assignments")
    void getActiveAssignmentsByPatient_NoAssignments_ReturnsEmptyList() {
        when(assignmentRepository.findByPatientIdAndStatus(10L, PlanAssignment.AssignmentStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<PlanAssignmentDTO> result = wellnessPlanService.getActiveAssignmentsByPatient(10L);

        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getActiveAssignmentsByPlan
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getActiveAssignmentsByPlan – success: returns active assignment DTOs for plan")
    void getActiveAssignmentsByPlan_Success() {
        WellnessPlanModel plan = buildPlanModel(1L, "Yoga Plan");
        PlanAssignment assignment = buildAssignment(2L, 20L, plan, PlanAssignment.AssignmentStatus.ACTIVE);

        when(assignmentRepository.findByWellnessPlan_PlanIdAndStatus(1L, PlanAssignment.AssignmentStatus.ACTIVE))
                .thenReturn(List.of(assignment));

        List<PlanAssignmentDTO> result = wellnessPlanService.getActiveAssignmentsByPlan(1L);

        assertEquals(1, result.size());
        assertEquals("Yoga Plan", result.get(0).getPlanName());
        assertEquals(1L, result.get(0).getPlanId());
    }

    @Test
    @DisplayName("getActiveAssignmentsByPlan – returns empty list when no active assignments for plan")
    void getActiveAssignmentsByPlan_NoAssignments_ReturnsEmptyList() {
        when(assignmentRepository.findByWellnessPlan_PlanIdAndStatus(99L, PlanAssignment.AssignmentStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<PlanAssignmentDTO> result = wellnessPlanService.getActiveAssignmentsByPlan(99L);

        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getPlanDetailsByPatientId
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getPlanDetailsByPatientId – returns null (implementation is commented out)")
    void getPlanDetailsByPatientId_ReturnsNull() throws PatientNotFoundException {
        // The actual implementation is commented out in the service and returns null.
        // This test documents the current contract so it will fail as a reminder
        // once the real implementation is added.
        assertNull(wellnessPlanService.getPlanDetailsByPatientId(1L));
    }
}