package com.pwms.report.integration;

import com.pwms.report.model.Report;
import com.pwms.report.model.Report.ReportStatus;
import com.pwms.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

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
class ReportRepositoryIntegrationTest {

    @Autowired
    private ReportRepository reportRepo;

    private Report report;

    @BeforeEach
    void setUp() {
        reportRepo.deleteAll();

        report = new Report();
        report.setPatientId(1);
        report.setPlanId(1);
        report.setGeneratedBy(1);
        report.setSummary("Patient: Ashok | Completed: 2/3 (66.67%)");
        report.setAdminSummary("Good progress overall.");
        report.setDate(LocalDate.now());
        report.setStatus(ReportStatus.PUBLISHED);
    }

    @Test
    void save_andFindByPatientId() {
        reportRepo.save(report);

        List<Report> result = reportRepo.findByPatientId(1);

        assertEquals(1, result.size());
        assertEquals("Ashok", result.get(0).getSummary()
                .split("\\|")[0].replace("Patient: ", "").trim());
    }

    @Test
    void findByPatientIdAndStatus_publishedOnly() {
        reportRepo.save(report);

        Report draft = new Report();
        draft.setPatientId(1);
        draft.setPlanId(1);
        draft.setGeneratedBy(1);
        draft.setSummary("Draft summary");
        draft.setDate(LocalDate.now());
        draft.setStatus(ReportStatus.DRAFT);
        reportRepo.save(draft);

        List<Report> published =
                reportRepo.findByPatientIdAndStatus(1, ReportStatus.PUBLISHED);

        assertEquals(1, published.size());
        assertEquals(ReportStatus.PUBLISHED, published.get(0).getStatus());
    }

    @Test
    void findByDateRange_returnsCorrectReports() {
        reportRepo.save(report);

        List<Report> result =
                reportRepo.findByPatientIdAndDateBetweenOrderByDateDesc(
                        1,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1));

        assertEquals(1, result.size());
    }

    @Test
    void findByDateRange_outsideRange_returnsEmpty() {
        reportRepo.save(report);

        List<Report> result =
                reportRepo.findByPatientIdAndDateBetweenOrderByDateDesc(
                        1,
                        LocalDate.now().minusDays(30),
                        LocalDate.now().minusDays(10));

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteReport_removesFromDB() {
        Report saved = reportRepo.save(report);
        reportRepo.deleteById(saved.getReportId());

        assertFalse(reportRepo.existsById(saved.getReportId()));
    }
}