package com.cts.WellnessPlanManagementModule.DTO.ResponseDTO;


import com.cts.WellnessPlanManagementModule.Model.WellnessPlanModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString

public class PatientReponseWithPlansDTO
{
    Long patientId;
    String patientName;
    WellnessPlanResponseDTO wellnessPlan;

    public PatientReponseWithPlansDTO(Long patientId, String patientName, WellnessPlanModel plans)
    {
        this.patientId=patientId;
        this.patientName=patientName;


       this.wellnessPlan=(plans!=null)?new WellnessPlanResponseDTO(plans):new WellnessPlanResponseDTO();
    }



}
