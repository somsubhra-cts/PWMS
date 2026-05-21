import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { ToastService } from '../../../core/services/toast.service';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { ProgressRecord } from '../../../core/models/progress.model';

@Component({
  selector: 'app-patient-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patient-history.component.html',
  styleUrls: ['./patient-history.component.css']
})
export class PatientHistoryComponent implements OnInit {
  private auth        = inject(AuthService);
  private planSvc     = inject(WellnessPlanService);
  private progressSvc = inject(ProgressService);
  private toast       = inject(ToastService);
  private route       = inject(ActivatedRoute);

  assignments = signal<PlanAssignment[]>([]);
  records     = signal<ProgressRecord[]>([]);
  selPlan     = signal<number | null>(null);
  selDate     = signal(new Date().toISOString().split('T')[0]);
  loading     = signal(false);
  searched    = signal(false);

  get patientId() { return this.auth.referenceId()!; }

  get grouped(): Map<string, ProgressRecord[]> {
    const map = new Map<string, ProgressRecord[]>();
    this.records().forEach(r => {
      const list = map.get(r.trackedDate) ?? [];
      list.push(r);
      map.set(r.trackedDate, list);
    });
    return map;
  }

  get groupedDates(): string[] { return Array.from(this.grouped.keys()).sort((a,b) => b.localeCompare(a)); }

  doneCount(recs: ProgressRecord[])    { return recs.filter(r => r.status === 'DONE').length; }
  skippedCount(recs: ProgressRecord[]) { return recs.filter(r => r.status === 'SKIPPED').length; }
  pct(recs: ProgressRecord[])          { return recs.length ? Math.round((this.doneCount(recs) / recs.length) * 100) : 0; }

  ngOnInit(): void {
    this.planSvc.getAssignmentsByPatient(this.patientId).subscribe({
      next: list => {
        this.assignments.set(list);
        const qPlan = this.route.snapshot.queryParamMap.get('planId');
        if (qPlan) { this.selPlan.set(+qPlan); this.search(); }
      },
      error: () => {}
    });
  }

  search(): void {
    const pid = this.patientId; const planId = this.selPlan(); const date = this.selDate();
    this.loading.set(true); this.searched.set(true);
    const obs = planId && date
      ? this.progressSvc.getByPatientPlanDate(pid, planId, date)
      : planId
        ? this.progressSvc.getByPatientAndPlan(pid, planId)
        : date
          ? this.progressSvc.getByPatientAndDate(pid, date)
          : this.progressSvc.getByPatient(pid);
    obs.subscribe({
      next: r => { this.records.set(r); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        // 404 = no records for the selected filter â€” empty state, not an error
        if (err?.status !== 404) {
          this.toast.error('Failed to load history.');
        }
      }
    });
  }

  statusIcon(s: string): string {
    return s === 'DONE' ? 'fas fa-check-circle' : s === 'SKIPPED' ? 'fas fa-forward' : 'fas fa-clock';
  }
}
