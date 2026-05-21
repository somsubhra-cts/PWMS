import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PlanAssignment } from '../../../core/models/wellness-plan.model';
import { ProgressSummary } from '../../../core/models/progress.model';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, DecimalPipe, RouterLink],
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  protected auth      = inject(AuthService);
  private planSvc     = inject(WellnessPlanService);
  private progressSvc = inject(ProgressService);
  protected notifSvc  = inject(NotificationService);

  assignments   = signal<PlanAssignment[]>([]);
  summaries     = signal<ProgressSummary[]>([]);
  notifications = signal<Notification[]>([]);
  loading       = signal(true);

  get patientId() { return this.auth.referenceId()!; }
  get activePlans() { return this.assignments().filter(a => a.status === 'ACTIVE'); }
  get avgCompletion() {
    const s = this.summaries();
    return s.length ? s.reduce((acc, x) => acc + x.completionPercentage, 0) / s.length : 0;
  }

  get circumference() { return 2 * Math.PI * 45; }
  dashOffset(pct: number) { return this.circumference - (pct / 100) * this.circumference; }
  ringColor(pct: number) { return pct >= 70 ? '#20c997' : pct >= 40 ? '#ffc107' : '#ef4444'; }

  ngOnInit(): void {
    const pid = this.patientId;
    this.planSvc.getAssignmentsByPatient(pid).subscribe({
      next: a => {
        this.assignments.set(a);
        a.filter(x => x.status === 'ACTIVE').forEach(x =>
          this.progressSvc.getSummary(pid, x.planId).subscribe({
            next: s => this.summaries.update(list => [...list, s]), error: () => {}
          })
        );
      }, error: () => {}
    });
    this.notifSvc.getForPatient(pid).subscribe({
      next: n => { this.notifications.set(n.slice(0, 5)); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  notifIcon(type: string): string {
    const map: Record<string, string> = {
      PLAN_ASSIGNED: 'fas fa-clipboard-check', ACTIVITY_APPRECIATION: 'fas fa-star',
      REPORT_SHARED: 'fas fa-file-medical', ACTIVITY_REMINDER: 'fas fa-bell',
      WEEKLY_SUMMARY: 'fas fa-chart-bar', APPOINTMENT_REMINDER: 'fas fa-calendar-check'
    };
    return map[type] ?? 'fas fa-bell';
  }

  notifColor(type: string): string {
    const map: Record<string, string> = {
      PLAN_ASSIGNED: 'purple', ACTIVITY_APPRECIATION: 'gold',
      REPORT_SHARED: 'cyan', ACTIVITY_REMINDER: 'warning',
      WEEKLY_SUMMARY: 'info', APPOINTMENT_REMINDER: 'teal'
    };
    return map[type] ?? 'info';
  }

  formatDate(d: string) {
    return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
