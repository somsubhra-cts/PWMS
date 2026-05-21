package com.pwms.progress.health;

import com.pwms.progress.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceHealthIndicator implements HealthIndicator {

    private final ProgressRepository progressRepo;

    @Override
    public Health health() {
        try {
            long count = progressRepo.count();
            log.debug("Health check — progress records: {}", count);
            return Health.up()
                    .withDetail("service",         "progress-service")
                    .withDetail("database",        "UP")
                    .withDetail("progressRecords", count)
                    .build();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service",  "progress-service")
                    .withDetail("database", "DOWN")
                    .withDetail("error",    e.getMessage())
                    .build();
        }
    }
}