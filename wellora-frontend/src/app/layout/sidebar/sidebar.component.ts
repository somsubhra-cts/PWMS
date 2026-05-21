import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  auth = inject(AuthService);

  adminNav: NavItem[] = [
    { label: 'Dashboard',      icon: 'fas fa-chart-pie',    route: '/admin/dashboard' },
    { label: 'Patients',       icon: 'fas fa-users',        route: '/admin/patients' },
    { label: 'Wellness Plans', icon: 'fas fa-clipboard-list', route: '/admin/plans' },
    { label: 'Progress',       icon: 'fas fa-chart-line',   route: '/admin/progress' },
    { label: 'Reports',        icon: 'fas fa-file-medical', route: '/admin/reports' },
    { label: 'Notifications',  icon: 'fas fa-bell',         route: '/admin/notifications' },
  ];

  patientNav: NavItem[] = [
    { label: 'Dashboard',      icon: 'fas fa-home',         route: '/patient/dashboard' },
    { label: 'My Plans',       icon: 'fas fa-clipboard-list', route: '/patient/plans' },
    { label: "Today's Activities", icon: 'fas fa-tasks',    route: '/patient/progress' },
    { label: 'History',        icon: 'fas fa-history',      route: '/patient/history' },
    { label: 'My Reports',     icon: 'fas fa-file-medical', route: '/patient/reports' },
    { label: 'Notifications',  icon: 'fas fa-bell',         route: '/patient/notifications' },
  ];

  get navItems(): NavItem[] {
    return this.auth.role() === 'ADMIN' ? this.adminNav : this.patientNav;
  }
}
