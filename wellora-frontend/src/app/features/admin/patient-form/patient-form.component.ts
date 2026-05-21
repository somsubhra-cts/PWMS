import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { PatientRequest } from '../../../core/models/patient.model';

@Component({
  selector: 'app-patient-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css']
})
export class PatientFormComponent implements OnInit {
  private svc   = inject(PatientService);
  private auth  = inject(AuthService);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private toast  = inject(ToastService);

  isEdit   = signal(false);
  patientId = signal<number | null>(null);
  loading  = signal(false);
  saving   = signal(false);

  form: PatientRequest = { patientName: '', age: 0, email: '', medicalHistory: '' };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.patientId.set(+id);
      this.loading.set(true);
      this.svc.getById(+id).subscribe({
        next: p => { this.form = { patientName: p.patientName, age: p.age, email: p.email, medicalHistory: p.medicalHistory ?? '' }; this.loading.set(false); },
        error: () => { this.toast.error('Patient not found.'); this.router.navigate(['/admin/patients']); }
      });
    }
  }

  save(): void {
    if (!this.form.patientName || !this.form.email || !this.form.age) {
      this.toast.error('Please fill all required fields.'); return;
    }
    this.saving.set(true);
    const payload: PatientRequest = { ...this.form, medicalHistory: this.form.medicalHistory || null };

    const obs = this.isEdit()
      ? this.svc.update(this.patientId()!, payload)
      : this.svc.create(payload);

    obs.subscribe({
      next: p => {
        this.saving.set(false);
        this.toast.success(this.isEdit() ? 'Patient updated successfully.' : 'Patient created successfully.');
        this.router.navigate(['/admin/patients', p.patientId]);
      },
      error: () => { this.toast.error('Failed to save patient.'); this.saving.set(false); }
    });
  }
}
