import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WellnessPlanService } from '../../../core/services/wellness-plan.service';
import { ToastService } from '../../../core/services/toast.service';
import { WellnessPlan, CreateWellnessPlanRequest } from '../../../core/models/wellness-plan.model';

@Component({
  selector: 'app-plans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './plans.component.html',
  styleUrls: ['./plans.component.css']
})
export class PlansComponent implements OnInit {
  private svc   = inject(WellnessPlanService);
  private toast = inject(ToastService);

  plans      = signal<WellnessPlan[]>([]);
  loading    = signal(true);
  showModal  = signal(false);
  editPlan   = signal<WellnessPlan | null>(null);
  saving     = signal(false);
  deleting   = signal<number | null>(null);
  expandedId = signal<number | null>(null);

  form: CreateWellnessPlanRequest = { planName: '', activityNames: [] };
  newActivity = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: d => { this.plans.set(d); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load plans.'); this.loading.set(false); }
    });
  }

  openCreate(): void {
    this.editPlan.set(null);
    this.form = { planName: '', activityNames: [] };
    this.newActivity = '';
    this.showModal.set(true);
  }

  openEdit(plan: WellnessPlan): void {
    this.editPlan.set(plan);
    this.form = { planName: plan.planName, activityNames: plan.activities.map(a => a.activityName) };
    this.newActivity = '';
    this.showModal.set(true);
  }

  addActivity(): void {
    const name = this.newActivity.trim();
    if (!name) return;
    this.form.activityNames = [...this.form.activityNames, name];
    this.newActivity = '';
  }

  removeActivity(i: number): void {
    this.form.activityNames = this.form.activityNames.filter((_, idx) => idx !== i);
  }

  save(): void {
    if (!this.form.planName.trim()) { this.toast.error('Plan name is required.'); return; }
    if (this.form.activityNames.length === 0) { this.toast.error('Add at least one activity.'); return; }
    this.saving.set(true);
    const obs = this.editPlan()
      ? this.svc.update(this.editPlan()!.planId, this.form)
      : this.svc.create(this.form);
    obs.subscribe({
      next: () => {
        this.toast.success(this.editPlan() ? 'Plan updated!' : 'Plan created!');
        this.showModal.set(false); this.saving.set(false); this.load();
      },
      error: () => { this.toast.error('Failed to save plan.'); this.saving.set(false); }
    });
  }

  delete(id: number, name: string): void {
    if (!confirm(`Delete plan "${name}"?`)) return;
    this.deleting.set(id);
    this.svc.delete(id).subscribe({
      next: () => { this.plans.update(l => l.filter(p => p.planId !== id)); this.toast.success('Plan deleted.'); this.deleting.set(null); },
      error: () => { this.toast.error('Failed to delete.'); this.deleting.set(null); }
    });
  }

  toggle(id: number): void { this.expandedId.set(this.expandedId() === id ? null : id); }
}
