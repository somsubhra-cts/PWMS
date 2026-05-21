import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { ProgressSummary } from '../../../core/models/progress.model';

@Component({
  selector: 'app-patient-plans',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './patient-plans.component.html',
  styleUrls: ['./patient-plans.component.css']
})
export class PatientPlansComponent implements OnInit {
  private auth        = inject(AuthService);
  private planSvc     = inject(WellnessPlanService);
  private progressSvc = inject(ProgressService);

  assignments = signal<PlanAssignment[]>([]);
  summaries   = signal<Map<number, ProgressSummary>>(new Map());
  loading     = signal(true);

  get patientId() { return this.auth.referenceId()!; }
  get activePlans()   { return this.assignments().filter(a => a.status === 'ACTIVE'); }
  get inactivePlans() { return this.assignments().filter(a => a.status === 'INACTIVE'); }

  ngOnInit(): void {
    this.planSvc.getAssignmentsByPatient(this.patientId).subscribe({
      next: list => {
        this.assignments.set(list);
        this.loading.set(false);
        list.forEach(a =>
          this.progressSvc.getSummary(this.patientId, a.planId).subscribe({
            next: s => this.summaries.update(m => { const nm = new Map(m); nm.set(a.planId, s); return nm; }),
            error: () => {}
          })
        );
      },
      error: () => this.loading.set(false)
    });
  }

  summary(planId: number): ProgressSummary | undefined { return this.summaries().get(planId); }

  get circumference() { return 2 * Math.PI * 45; }
  dashOffset(pct: number) { return this.circumference - (pct / 100) * this.circumference; }
  ringColor(pct: number)  { return pct >= 70 ? '#20c997' : pct >= 40 ? '#ffc107' : '#ef4444'; }
}
