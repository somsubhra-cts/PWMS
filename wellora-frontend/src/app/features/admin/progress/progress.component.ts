import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { ToastService } from '../../../core/services/toast.service';
import { Patient } from '../../../core/models/patient.model';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { ProgressRecord, ProgressSummary } from '../../../core/models/progress.model';

@Component({
  selector: 'app-admin-progress',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './progress.component.html',
  styleUrls: ['./progress.component.css']
})
export class AdminProgressComponent implements OnInit {
  private patientSvc = inject(PatientService);
  private planSvc    = inject(WellnessPlanService);
  private progressSvc= inject(ProgressService);
  private toast      = inject(ToastService);
  private route      = inject(ActivatedRoute);

  patients    = signal<Patient[]>([]);
  assignments = signal<PlanAssignment[]>([]);
  records     = signal<ProgressRecord[]>([]);
  summary     = signal<ProgressSummary | null>(null);

  selPatient = signal<number | null>(null);
  selPlan    = signal<number | null>(null);
  selDate    = signal(new Date().toISOString().split('T')[0]);
  loading    = signal(false);

  ngOnInit(): void {
    this.patientSvc.getAll().subscribe({ next: d => { this.patients.set(d); }, error: () => {} });
    const qId = this.route.snapshot.queryParamMap.get('patientId');
    if (qId) { this.selPatient.set(+qId); this.onPatientChange(); }
  }

  onPatientChange(): void {
    const pid = this.selPatient();
    if (!pid) return;
    this.selPlan.set(null); this.records.set([]); this.summary.set(null);
    this.planSvc.getAssignmentsByPatient(pid).subscribe({ next: a => this.assignments.set(a), error: () => {} });
  }

  search(): void {
    const pid = this.selPatient(); const planId = this.selPlan(); const date = this.selDate();
    if (!pid) { this.toast.warning('Select a patient.'); return; }
    this.loading.set(true);
    const obs = planId && date
      ? this.progressSvc.getByPatientPlanDate(pid, planId, date)
      : planId
        ? this.progressSvc.getByPatientAndPlan(pid, planId)
        : date
          ? this.progressSvc.getByPatientAndDate(pid, date)
          : this.progressSvc.getByPatient(pid);
    obs.subscribe({
      next: r => { this.records.set(r); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load progress.'); this.loading.set(false); }
    });
    if (planId) {
      this.progressSvc.getSummary(pid, planId).subscribe({ next: s => this.summary.set(s), error: () => {} });
    }
  }

  get circumference() { return 2 * Math.PI * 45; }
  get dashOffset() {
    const pct = this.summary()?.completionPercentage ?? 0;
    return this.circumference - (pct / 100) * this.circumference;
  }

  statusColor(s: string): string {
    return s === 'DONE' ? 'success' : s === 'SKIPPED' ? 'muted' : 'warning';
  }
}
