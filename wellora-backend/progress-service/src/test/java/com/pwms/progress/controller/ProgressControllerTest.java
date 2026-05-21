package com.pwms.progress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwms.progress.dto.*;
import com.pwms.progress.exception.ProgressNotFoundException;
import com.pwms.progress.interfaces.ProgressIntf;
import com.pwms.progress.model.Progress.ActivityStatus;
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

@WebMvcTest(ProgressController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false",
        "spring.cloud.config.enabled=false"
})
class ProgressControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean
    private ProgressIntf progressService;

    private ProgressResponseDTO responseDTO;
    private ProgressSummaryDTO  summaryDTO;

    @BeforeEach
    void setUp() {
        responseDTO = ProgressResponseDTO.builder()
                .progressId(1)
                .patientId(1)
                .planId(1)
                .activityId(1)
                .activityName("Walking")
                .status("PENDING")
                .trackedDate(LocalDate.now())
                .build();

        summaryDTO = ProgressSummaryDTO.builder()
                .patientId(1)
                .planId(1)
                .date(LocalDate.now())
                .totalActivities(3)
                .completedActivities(2)
                .completionPercentage(66.67)
                .build();
    }

    // ── POST /api/progress/init ───────────────────────────────

    @Test
    void initializeProgress_returns201() throws Exception {
        doNothing().when(progressService).initializeProgress(1, 1);

        mockMvc.perform(post("/api/progress/init")
                        .param("patientId", "1")
                        .param("planId", "1"))
                .andExpect(status().isCreated())
                .andExpect(content().string(
                        "Progress initialized for patientId: 1 planId: 1"));
    }

    // ── PATCH /api/progress/update/{patientId} ────────────────

    @Test
    void updateStatus_returns200() throws Exception {
        StatusUpdateDTO dto = new StatusUpdateDTO(1, ActivityStatus.DONE);
        responseDTO.setStatus("DONE");

        when(progressService.updateStatus(eq(1), any(StatusUpdateDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/progress/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.activityName").value("Walking"));
    }

    @Test
    void updateStatus_notFound_returns404() throws Exception {
        StatusUpdateDTO dto = new StatusUpdateDTO(99, ActivityStatus.DONE);

        when(progressService.updateStatus(eq(1), any(StatusUpdateDTO.class)))
                .thenThrow(new ProgressNotFoundException(
                        "No progress row found for activityId: 99"));

        mockMvc.perform(patch("/api/progress/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/progress/patient/{patientId} ─────────────────

    @Test
    void getByPatient_returns200() throws Exception {
        when(progressService.getProgressByPatient(1))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/progress/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityName").value("Walking"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByPatient_notFound_returns404() throws Exception {
        when(progressService.getProgressByPatient(99))
                .thenThrow(new ProgressNotFoundException(
                        "No progress found for patientId: 99"));

        mockMvc.perform(get("/api/progress/patient/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/progress/summary/{patientId}/plan/{planId} ───

    @Test
    void getDailySummary_returns200() throws Exception {
        when(progressService.getDailySummary(1, 1))
                .thenReturn(summaryDTO);

        mockMvc.perform(get("/api/progress/summary/1/plan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(3))
                .andExpect(jsonPath("$.completedActivities").value(2))
                .andExpect(jsonPath("$.completionPercentage").value(66.67));
    }
}