package com.pwms.progress.integration;

import com.pwms.progress.model.Progress;
import com.pwms.progress.model.Progress.ActivityStatus;
import com.pwms.progress.repository.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false"
})
class ProgressRepositoryIntegrationTest {

    @Autowired
    private ProgressRepository progressRepo;

    private Progress progress;

    @BeforeEach
    void setUp() {
        progressRepo.deleteAll();

        progress = Progress.builder()
                .patientId(1)
                .planId(1)
                .activityId(1)
                .status(ActivityStatus.PENDING)
                .trackedDate(LocalDate.now())
                .build();
    }

    @Test
    void save_andFindByPatientId() {
        progressRepo.save(progress);

        List<Progress> result = progressRepo.findByPatientId(1);

        assertEquals(1, result.size());
        assertEquals(ActivityStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void findByPatientIdAndPlanId() {
        progressRepo.save(progress);

        List<Progress> result =
                progressRepo.findByPatientIdAndPlanId(1, 1);

        assertEquals(1, result.size());
    }

    @Test
    void findByPatientIdAndActivityIdAndTrackedDate() {
        progressRepo.save(progress);

        Optional<Progress> result =
                progressRepo.findByPatientIdAndActivityIdAndTrackedDate(
                        1, 1, LocalDate.now());

        assertTrue(result.isPresent());
        assertEquals(ActivityStatus.PENDING, result.get().getStatus());
    }

    @Test
    void uniqueConstraint_preventsduplicateEntry() {
        progressRepo.save(progress);

        Progress duplicate = Progress.builder()
                .patientId(1)
                .planId(1)
                .activityId(1)
                .status(ActivityStatus.DONE)
                .trackedDate(LocalDate.now())
                .build();

        assertThrows(Exception.class,
                () -> progressRepo.saveAndFlush(duplicate));
    }

    @Test
    void countByStatusForDate_returnsCorrectCount() {
        progressRepo.save(progress);

        Progress done = Progress.builder()
                .patientId(1)
                .planId(1)
                .activityId(2)
                .status(ActivityStatus.DONE)
                .trackedDate(LocalDate.now())
                .build();
        progressRepo.save(done);

        long count = progressRepo.countByStatusForDate(
                1, 1, ActivityStatus.DONE, LocalDate.now());

        assertEquals(1L, count);
    }

    @Test
    void findByDateRange() {
        progressRepo.save(progress);

        List<Progress> result =
                progressRepo.findByPatientIdAndPlanIdAndTrackedDateBetween(
                        1, 1,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1));

        assertEquals(1, result.size());
    }
}