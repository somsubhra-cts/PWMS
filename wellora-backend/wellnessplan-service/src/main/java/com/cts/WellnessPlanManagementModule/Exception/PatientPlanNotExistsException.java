package com.cts.WellnessPlanManagementModule.Exception;

import io.micrometer.observation.annotation.Observed;

public class PatientPlanNotExistsException extends Exception
{
    public PatientPlanNotExistsException(String errorMessage)
    {
        super(errorMessage);
    }
}
