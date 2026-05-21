import { Routes } from '@angular/router';
import { authGuard }    from './core/guards/auth.guard';
import { adminGuard }   from './core/guards/admin.guard';
import { patientGuard } from './core/guards/patient.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },

  {
    path: 'admin',
    loadComponent: () =>
      import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'patients',
        loadComponent: () =>
          import('./features/admin/patients/patients.component').then(m => m.PatientsComponent)
      },
      {
        path: 'patients/new',
        loadComponent: () =>
          import('./features/admin/patient-form/patient-form.component').then(m => m.PatientFormComponent)
      },
      {
        path: 'patients/:id/edit',
        loadComponent: () =>
          import('./features/admin/patient-form/patient-form.component').then(m => m.PatientFormComponent)
      },
      {
        path: 'patients/:id',
        loadComponent: () =>
          import('./features/admin/patient-detail/patient-detail.component').then(m => m.PatientDetailComponent)
      },
      {
        path: 'plans',
        loadComponent: () =>
          import('./features/admin/plans/plans.component').then(m => m.PlansComponent)
      },
      {
        path: 'progress',
        loadComponent: () =>
          import('./features/admin/progress/progress.component').then(m => m.AdminProgressComponent)
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./features/admin/reports/reports.component').then(m => m.AdminReportsComponent)
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/admin/notifications/notifications.component').then(m => m.AdminNotificationsComponent)
      }
    ]
  },

  {
    path: 'patient',
    loadComponent: () =>
      import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard, patientGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/patient/dashboard/patient-dashboard.component').then(m => m.PatientDashboardComponent)
      },
      {
        path: 'plans',
        loadComponent: () =>
          import('./features/patient/plans/patient-plans.component').then(m => m.PatientPlansComponent)
      },
      {
        path: 'progress',
        loadComponent: () =>
          import('./features/patient/progress/patient-progress.component').then(m => m.PatientProgressComponent)
      },
      {
        path: 'history',
        loadComponent: () =>
          import('./features/patient/history/patient-history.component').then(m => m.PatientHistoryComponent)
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./features/patient/reports/patient-reports.component').then(m => m.PatientReportsComponent)
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/patient/notifications/patient-notifications.component').then(m => m.PatientNotificationsComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
