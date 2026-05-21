import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-host">
      @for (toast of toastSvc.toasts(); track toast.id) {
        <div class="toast toast-{{ toast.type }}" (click)="toastSvc.dismiss(toast.id)">
          <i [class]="iconClass(toast.type)"></i>
          <span>{{ toast.message }}</span>
        </div>
      }
    </div>
  `
})
export class ToastHostComponent {
  toastSvc = inject(ToastService);

  iconClass(type: string): string {
    const map: Record<string, string> = {
      success: 'fas fa-check-circle',
      error:   'fas fa-times-circle',
      info:    'fas fa-info-circle',
      warning: 'fas fa-exclamation-triangle'
    };
    return map[type] ?? 'fas fa-info-circle';
  }
}
