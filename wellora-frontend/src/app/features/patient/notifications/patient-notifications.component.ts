import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ToastService } from '../../../core/services/toast.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-patient-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './patient-notifications.component.html',
  styleUrls: ['./patient-notifications.component.css']
})
export class PatientNotificationsComponent implements OnInit {
  private auth    = inject(AuthService);
  private notifSvc = inject(NotificationService);
  private toast   = inject(ToastService);

  notifications = signal<Notification[]>([]);
  loading       = signal(true);
  filter        = signal<'ALL' | 'UNREAD'>('ALL');

  get patientId() { return this.auth.referenceId()!; }

  displayed = computed(() =>
    this.filter() === 'UNREAD'
      ? this.notifications().filter(n => !n.isRead)
      : this.notifications()
  );

  unreadCount = computed(() => this.notifications().filter(n => !n.isRead).length);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.notifSvc.getForPatient(this.patientId).subscribe({
      next:  n => {
        // sort most recent first
        const sorted = [...n].sort((a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.notifications.set(sorted);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        // 404 = no notifications yet â€” empty state, not an error
        if (err?.status !== 404) {
          this.toast.error('Failed to load notifications.');
        }
      }
    });
  }

  markRead(n: Notification): void {
    if (n.isRead) return;
    this.notifSvc.markAsRead(n.notificationId).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(x => x.notificationId === n.notificationId ? { ...x, isRead: true } : x)
        );
        this.notifSvc.decrementUnread();
      },
      error: () => this.toast.error('Could not mark as read.')
    });
  }

  markAllRead(): void {
    const unread = this.notifications().filter(n => !n.isRead);
    unread.forEach(n => this.markRead(n));
  }

  typeIcon(t: string): string {
    const map: Record<string, string> = {
      PLAN_ASSIGNED:      'fas fa-clipboard-list',
      PLAN_UPDATED:       'fas fa-edit',
      PLAN_REMOVED:       'fas fa-trash',
      PROGRESS_REMINDER:  'fas fa-bell',
      REPORT_PUBLISHED:   'fas fa-file-medical',
      GENERAL:            'fas fa-info-circle'
    };
    return map[t] ?? 'fas fa-bell';
  }

  typeColor(t: string): string {
    const map: Record<string, string> = {
      PLAN_ASSIGNED:      'var(--primary-light)',
      PLAN_UPDATED:       'var(--cyan)',
      PLAN_REMOVED:       'var(--danger)',
      PROGRESS_REMINDER:  'var(--warning)',
      REPORT_PUBLISHED:   'var(--success)',
      GENERAL:            'var(--text-muted)'
    };
    return map[t] ?? 'var(--text-muted)';
  }
}
