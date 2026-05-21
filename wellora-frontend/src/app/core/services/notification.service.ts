import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/notifications`;

  private _unreadCount = signal(0);
  readonly unreadCount = this._unreadCount.asReadonly();

  getForPatient(patientId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/patient/${patientId}`);
  }

  getUnreadForPatient(patientId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/patient/${patientId}/unread`);
  }

  getForAdmin(adminId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/admin/${adminId}`);
  }

  getUnreadForAdmin(adminId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/admin/${adminId}/unread`);
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.base}/${notificationId}/read`, null);
  }

  refreshUnreadCount(id: number, role: 'ADMIN' | 'PATIENT'): void {
    const obs = role === 'ADMIN'
      ? this.getUnreadForAdmin(id)
      : this.getUnreadForPatient(id);
    obs.subscribe({
      next: list => this._unreadCount.set(list.length),
      error: err => { if (err?.status === 404) this._unreadCount.set(0); }
    });
  }

  decrementUnread(): void {
    this._unreadCount.update(n => Math.max(0, n - 1));
  }
}
