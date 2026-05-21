package com.pwms.report.health;

import com.pwms.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportServiceHealthIndicator implements HealthIndicator {

    private final ReportRepository reportRepo;

    @Override
    public Health health() {
        try {
            long count = reportRepo.count();
            log.debug("Health check — report count: {}", count);
            return Health.up()
                    .withDetail("service",      "report-service")
                    .withDetail("database",     "UP")
                    .withDetail("reportCount",  count)
                    .build();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service",  "report-service")
                    .withDetail("database", "DOWN")
                    .withDetail("error",    e.getMessage())
                    .build();
        }
    }
}