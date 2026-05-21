import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ProgressService } from '../../../core/services/progress.service';
import { ToastService } from '../../../core/services/toast.service';
import { Patient } from '../../../core/models/patient.model';
import { WellnessPlan, PlanAssignment } from '../../../core/models/wellness-plan.model';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './patient-detail.component.html',
  styleUrls: ['./patient-detail.component.css']
})
export class PatientDetailComponent implements OnInit {
  private patientSvc = inject(PatientService);
  private planSvc    = inject(WellnessPlanService);
  private progressSvc= inject(ProgressService);
  private route      = inject(ActivatedRoute);
  private toast      = inject(ToastService);

  patient     = signal<Patient | null>(null);
  assignments = signal<PlanAssignment[]>([]);
  allPlans    = signal<WellnessPlan[]>([]);
  loading     = signal(true);
  showModal   = signal(false);
  selectedPlanId = signal<number | null>(null);
  assigning   = signal(false);

  get patientId(): number { return +this.route.snapshot.paramMap.get('id')!; }

  ngOnInit(): void {
    this.patientSvc.getById(this.patientId).subscribe({
      next: p => { this.patient.set(p); this.loading.set(false); },
      error: () => { this.toast.error('Patient not found.'); this.loading.set(false); }
    });
    this.planSvc.getAssignmentsByPatient(this.patientId).subscribe({
      next: a => this.assignments.set(a), error: () => {}
    });
    this.planSvc.getAll().subscribe({ next: p => this.allPlans.set(p), error: () => {} });
  }

  openAssignModal(): void { this.selectedPlanId.set(null); this.showModal.set(true); }

  assignPlan(): void {
    if (!this.selectedPlanId()) { this.toast.error('Select a plan to assign.'); return; }
    this.assigning.set(true);
    const planId = this.selectedPlanId()!;
    this.planSvc.assign(this.patientId, planId).subscribe({
      next: () => {
        // init seeds first-day progress; ignore 409 if already initialized
        this.progressSvc.init(this.patientId, planId).subscribe({ error: () => {} });
        this.toast.success('Plan assigned successfully!');
        this.showModal.set(false);
        this.assigning.set(false);
        this.planSvc.getAssignmentsByPatient(this.patientId)
          .subscribe({ next: a => this.assignments.set(a), error: () => {} });
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Failed to assign plan.';
        this.toast.error(msg);
        this.assigning.set(false);
      }
    });
  }

  initials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }
}
