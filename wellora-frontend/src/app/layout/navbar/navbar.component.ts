import { Component, inject, Input, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  @Input() unreadCount = 0;
  auth = inject(AuthService);

  get notifRoute(): string {
    return this.auth.role() === 'ADMIN' ? '/admin/notifications' : '/patient/notifications';
  }
}
