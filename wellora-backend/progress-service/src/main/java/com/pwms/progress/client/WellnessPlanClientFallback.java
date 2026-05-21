package com.pwms.progress.client;

import com.pwms.progress.dto.ActivityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WellnessPlanClientFallback implements WellnessPlanClient {

    @Override
    public List<ActivityDTO> getActivitiesByPlanId(int planId) {
        log.warn("Circuit OPEN — wellnessplan-service unavailable. " +
                "Returning empty activities for planId: {}", planId);
        return List.of();  // empty list — progress init will fail gracefully
    }

    @Override
    public Object getPlanById(int planId) {
        log.warn("Circuit OPEN — wellnessplan-service unavailable. " +
                "Cannot verify planId: {}", planId);
        return null;
    }
}
