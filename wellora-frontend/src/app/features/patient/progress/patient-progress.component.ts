import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { ToastService } from '../../../core/services/toast.service';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { ProgressRecord, ProgressStatus, ProgressSummary } from '../../../core/models/progress.model';

@Component({
  selector: 'app-patient-progress',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe],
  templateUrl: './patient-progress.component.html',
  styleUrls: ['./patient-progress.component.css']
})
export class PatientProgressComponent implements OnInit {
  private auth        = inject(AuthService);
  private planSvc     = inject(WellnessPlanService);
  private progressSvc = inject(ProgressService);
  private toast       = inject(ToastService);
  private route       = inject(ActivatedRoute);

  assignments  = signal<PlanAssignment[]>([]);
  records      = signal<ProgressRecord[]>([]);
  summary      = signal<ProgressSummary | null>(null);
  selPlan      = signal<number | null>(null);
  loading      = signal(false);
  updating     = signal<number | null>(null);
  today        = new Date().toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

  get patientId() { return this.auth.referenceId()!; }
  get circumference() { return 2 * Math.PI * 45; }
  get dashOffset() {
    const pct = this.summary()?.completionPercentage ?? 0;
    return this.circumference - (pct / 100) * this.circumference;
  }
  ringColor(pct: number) { return pct >= 70 ? '#20c997' : pct >= 40 ? '#ffc107' : '#ef4444'; }

  ngOnInit(): void {
    this.planSvc.getAssignmentsByPatient(this.patientId).subscribe({
      next: list => {
        this.assignments.set(list.filter(a => a.status === 'ACTIVE'));
        const qPlan = this.route.snapshot.queryParamMap.get('planId');
        const first = qPlan ? +qPlan : list.find(a => a.status === 'ACTIVE')?.planId ?? null;
        if (first) { this.selPlan.set(first); this.loadToday(); }
      },
      error: () => {}
    });
  }

  loadToday(): void {
    const planId = this.selPlan(); if (!planId) return;
    this.loading.set(true);
    this.progressSvc.seed(this.patientId, planId).subscribe({
      next: r => { this.records.set(r); this.loading.set(false); this.loadSummary(); },
      error: () => { this.toast.error('Failed to load activities.'); this.loading.set(false); }
    });
  }

  loadSummary(): void {
    const planId = this.selPlan(); if (!planId) return;
    this.progressSvc.getSummary(this.patientId, planId).subscribe({
      next: s => this.summary.set(s), error: () => {}
    });
  }

  onPlanChange(): void { this.records.set([]); this.summary.set(null); this.loadToday(); }

  updateStatus(r: ProgressRecord, status: ProgressStatus): void {
    if (r.status === status) return;
    this.updating.set(r.progressId);
    this.progressSvc.updateStatus(this.patientId, { activityId: r.activityId, status, planId: r.planId }).subscribe({
      next: updated => {
        this.records.update(list => list.map(x => x.progressId === updated.progressId ? updated : x));
        this.updating.set(null);
        this.loadSummary();
        if (status === 'DONE') this.toast.success('Great job! Activity marked as done! ðŸŽ‰');
      },
      error: () => { this.toast.error('Failed to update status.'); this.updating.set(null); }
    });
  }
}
