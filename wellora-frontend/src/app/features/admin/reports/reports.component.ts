import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ReportService } from '../../../core/services/report.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { Patient } from '../../../core/models/patient.model';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { Report, ReportPreview } from '../../../core/models/report.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class AdminReportsComponent implements OnInit {
  private patientSvc = inject(PatientService);
  private planSvc    = inject(WellnessPlanService);
  private reportSvc  = inject(ReportService);
  private toast      = inject(ToastService);
  private auth       = inject(AuthService);
  private route      = inject(ActivatedRoute);

  patients    = signal<Patient[]>([]);
  assignments = signal<PlanAssignment[]>([]);
  reports     = signal<Report[]>([]);
  preview     = signal<ReportPreview | null>(null);

  selPatient  = signal<number | null>(null);
  selPlan     = signal<number | null>(null);
  adminSummary = signal('');

  loading      = signal(false);
  previewing   = signal(false);
  generating   = signal(false);
  downloading  = signal<number | null>(null);
  showGenModal = signal(false);

  ngOnInit(): void {
    this.patientSvc.getAll().subscribe({ next: d => this.patients.set(d), error: () => {} });
    const qId = this.route.snapshot.queryParamMap.get('patientId');
    if (qId) { this.selPatient.set(+qId); this.onPatientChange(); this.loadReports(); }
  }

  onPatientChange(): void {
    const pid = this.selPatient(); if (!pid) return;
    this.selPlan.set(null); this.preview.set(null);
    this.planSvc.getAssignmentsByPatient(pid).subscribe({ next: a => this.assignments.set(a), error: () => {} });
    this.loadReports();
  }

  loadReports(): void {
    const pid = this.selPatient(); if (!pid) return;
    this.loading.set(true);
    this.reportSvc.getAllByPatientAdmin(pid).subscribe({
      next: d => { this.reports.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  loadPreview(): void {
    const pid = this.selPatient(), planId = this.selPlan();
    if (!pid || !planId) { this.toast.warning('Select a patient and plan.'); return; }
    this.previewing.set(true);
    this.reportSvc.preview(pid, planId).subscribe({
      next: p => { this.preview.set(p); this.previewing.set(false); this.showGenModal.set(true); },
      error: () => { this.toast.error('Preview failed.'); this.previewing.set(false); }
    });
  }

  generate(): void {
    const pid = this.selPatient(), planId = this.selPlan();
    if (!pid || !planId) return;
    this.generating.set(true);
    this.reportSvc.generate(pid, planId, { adminId: this.auth.referenceId()!, adminSummary: this.adminSummary() }).subscribe({
      next: () => {
        this.toast.success('Report generated and published!');
        this.showGenModal.set(false); this.generating.set(false);
        this.adminSummary.set(''); this.preview.set(null);
        this.loadReports();
      },
      error: () => { this.toast.error('Failed to generate report.'); this.generating.set(false); }
    });
  }

  download(reportId: number): void {
    this.downloading.set(reportId);
    this.reportSvc.downloadPdf(reportId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `report-${reportId}.pdf`; a.click();
        URL.revokeObjectURL(url);
        this.downloading.set(null);
      },
      error: () => { this.toast.error('Download failed.'); this.downloading.set(null); }
    });
  }

  get circumference() { return 2 * Math.PI * 45; }
  dashOffset(pct: number) { return this.circumference - (pct / 100) * this.circumference; }
  ringColor(pct: number) { return pct >= 70 ? '#20c997' : pct >= 40 ? '#ffc107' : '#ef4444'; }
  formatDate(d: string) { return new Date(d).toLocaleDateString('en-US', { year:'numeric', month:'short', day:'numeric' }); }
}
