import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.css']
})
export class ShellComponent implements OnInit {
  private auth = inject(AuthService);
  notif        = inject(NotificationService);

  ngOnInit(): void {
    this.loadUnread();
    setInterval(() => this.loadUnread(), 30000);
  }

  private loadUnread(): void {
    const id   = this.auth.referenceId();
    const role = this.auth.role();
    if (!id) return;
    this.notif.refreshUnreadCount(id, role as 'ADMIN' | 'PATIENT');
  }
}
