import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PatientService } from '../../../core/services/patient.service';
import { ToastService } from '../../../core/services/toast.service';
import { Patient } from '../../../core/models/patient.model';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './patients.component.html',
  styleUrls: ['./patients.component.css']
})
export class PatientsComponent implements OnInit {
  private svc   = inject(PatientService);
  private toast = inject(ToastService);

  patients  = signal<Patient[]>([]);
  loading   = signal(true);
  search    = signal('');
  deleting  = signal<number | null>(null);

  get filtered() {
    const q = this.search().toLowerCase();
    return q ? this.patients().filter(p => p.patientName.toLowerCase().includes(q) || p.email.toLowerCase().includes(q)) : this.patients();
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: d => { this.patients.set(d); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load patients.'); this.loading.set(false); }
    });
  }

  delete(id: number, name: string): void {
    if (!confirm(`Delete patient "${name}"? This cannot be undone.`)) return;
    this.deleting.set(id);
    this.svc.delete(id).subscribe({
      next: () => {
        this.patients.update(list => list.filter(p => p.patientId !== id));
        this.toast.success(`Patient "${name}" deleted.`);
        this.deleting.set(null);
      },
      error: () => { this.toast.error('Failed to delete patient.'); this.deleting.set(null); }
    });
  }

  initials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }
}
