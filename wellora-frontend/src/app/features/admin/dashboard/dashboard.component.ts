import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { Patient } from '../../../core/models/patient.model';
import { WellnessPlan } from '../../../core/models/wellness-plan.model';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  private patientSvc = inject(PatientService);
  private planSvc    = inject(WellnessPlanService);
  protected notifSvc = inject(NotificationService);
  private auth       = inject(AuthService);

  patients      = signal<Patient[]>([]);
  plans         = signal<WellnessPlan[]>([]);
  notifications = signal<Notification[]>([]);
  loading       = signal(true);

  get totalPatients()      { return this.patients().length; }
  get totalPlans()         { return this.plans().length; }
  get recentPatients()     { return this.patients().slice(-5).reverse(); }
  get recentNotifications(){ return this.notifications().slice(0, 5); }

  ngOnInit(): void {
    const id = this.auth.referenceId()!;
    this.patientSvc.getAll().subscribe({ next: d => this.patients.set(d), error: () => {} });
    this.planSvc.getAll().subscribe({ next: d => this.plans.set(d), error: () => {} });
    this.notifSvc.getForAdmin(id).subscribe({
      next: d => { this.notifications.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  notifIcon(type: string): string {
    const map: Record<string, string> = {
      NEW_PATIENT_REGISTERED:  'fas fa-user-plus',
      PLAN_COMPLETED:          'fas fa-check-circle',
      GENERATE_REPORT_REMINDER:'fas fa-file-medical',
    };
    return map[type] ?? 'fas fa-bell';
  }

  notifColor(type: string): string {
    const map: Record<string, string> = {
      NEW_PATIENT_REGISTERED:  'cyan',
      PLAN_COMPLETED:          'success',
      GENERATE_REPORT_REMINDER:'warning',
    };
    return map[type] ?? 'info';
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
