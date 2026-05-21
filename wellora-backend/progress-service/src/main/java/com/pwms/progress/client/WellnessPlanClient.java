package com.pwms.progress.client;

import com.pwms.progress.dto.ActivityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name     = "wellnessplan-service",
        path     = "/api/plans",
        fallback = WellnessPlanClientFallback.class
)
public interface WellnessPlanClient {

    @GetMapping("/{planId}/activities")
    List<ActivityDTO> getActivitiesByPlanId(@PathVariable int planId);

    @GetMapping("/{planId}")
    Object getPlanById(@PathVariable int planId);
}
