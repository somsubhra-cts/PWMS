package com.cts.WellnessPlanManagementModule.DTO.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CreateWellnessPlanDTO
{
    String planName;


    List<String> activityNames;

}

