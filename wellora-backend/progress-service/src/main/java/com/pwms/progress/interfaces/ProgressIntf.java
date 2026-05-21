package com.pwms.progress.interfaces;

import com.pwms.progress.dto.*;
import com.pwms.progress.exception.*;

import java.time.LocalDate;
import java.util.List;

public interface ProgressIntf {

    void initializeProgress(int patientId, int planId)
            throws ProgressAlreadyExistsException;

    List<ProgressResponseDTO> seedProgressForDate(int patientId, int planId);

    ProgressResponseDTO updateStatus(int patientId, StatusUpdateDTO dto)
            throws ProgressNotFoundException;

    List<ProgressResponseDTO> getProgressByPatient(int patientId)
            throws ProgressNotFoundException;

    List<ProgressResponseDTO> getProgressByPatientAndPlan(int patientId, int planId)
            throws ProgressNotFoundException;

    ProgressSummaryDTO getDailySummary(int patientId, int planId)
            throws ProgressNotFoundException;

    List<ProgressResponseDTO> getProgressByPatientAndPlanAndDate(
            int patientId, int planId, LocalDate date)
            throws ProgressNotFoundException;

    List<ProgressResponseDTO> getProgressByPatientAndDate(
            int patientId, LocalDate date)
            throws ProgressNotFoundException;
}