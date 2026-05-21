package com.cts.WellnessPlanManagementModule.DTO.ResponseDTO;

import com.cts.WellnessPlanManagementModule.Model.ActivitiesModel;
import com.cts.WellnessPlanManagementModule.Model.WellnessPlanModel;
import lombok.*;

import java.util.ArrayList;
import java.util.List;



@Data
@ToString
@NoArgsConstructor
public class WellnessPlanResponseDTO
{
    private Long planId;
    private String planName;

    private List<ActivitiesDTO> activities=new ArrayList<>();
    public WellnessPlanResponseDTO(WellnessPlanModel plan)
    {
        this.planId=plan.getPlanId();
        this.planName=plan.getPlanName();
        for(ActivitiesModel activity:plan.getActivities())
        {
           ActivitiesDTO dto=new ActivitiesDTO(activity.getActivityId(),activity.getActivityDescription());
           activities.add(dto);

        }
    }






}