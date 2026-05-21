import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class AdminNotificationsComponent implements OnInit {
  private svc   = inject(NotificationService);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);

  notifications = signal<Notification[]>([]);
  loading       = signal(true);
  filter        = signal<'ALL' | 'UNREAD'>('ALL');

  get adminId() { return this.auth.referenceId()!; }
  get displayed() {
    return this.filter() === 'UNREAD'
      ? this.notifications().filter(n => !n.isRead)
      : this.notifications();
  }
  get unreadCount() { return this.notifications().filter(n => !n.isRead).length; }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getForAdmin(this.adminId).subscribe({
      next: d => { this.notifications.set(d.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  markRead(n: Notification): void {
    if (n.isRead) return;
    this.svc.markAsRead(n.notificationId).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(x => x.notificationId === n.notificationId ? { ...x, isRead: true } : x)
        );
        this.svc.decrementUnread();
      },
      error: () => this.toast.error('Failed to mark as read.')
    });
  }

  markAllRead(): void {
    const unread = this.notifications().filter(n => !n.isRead);
    unread.forEach(n => this.markRead(n));
    this.toast.success('All marked as read.');
  }

  icon(type: string): string {
    const map: Record<string,string> = {
      NEW_PATIENT_REGISTERED: 'fas fa-user-plus', PLAN_COMPLETED: 'fas fa-check-circle',
      GENERATE_REPORT_REMINDER: 'fas fa-file-medical'
    };
    return map[type] ?? 'fas fa-bell';
  }

  color(type: string): string {
    const map: Record<string,string> = {
      NEW_PATIENT_REGISTERED: 'teal', PLAN_COMPLETED: 'success', GENERATE_REPORT_REMINDER: 'warning'
    };
    return map[type] ?? 'info';
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleString('en-US', { month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' });
  }
}
