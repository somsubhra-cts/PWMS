import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private auth   = inject(AuthService);
  private router = inject(Router);
  private toast  = inject(ToastService);

  tab       = signal<'login' | 'register'>('login');

  // Login fields
  username  = signal('');
  password  = signal('');
  loading   = signal(false);
  showPass  = signal(false);

  // Register fields
  regUsername    = signal('');
  regPassword    = signal('');
  regConfirm     = signal('');
  regReferenceId = signal('');
  regEmail       = signal('');   // must match email the admin registered for this patient
  regLoading     = signal(false);
  showRegPass    = signal(false);

  features = [
    { icon: 'fas fa-users',         label: 'Patient Management',    desc: 'Register and manage patient profiles' },
    { icon: 'fas fa-clipboard-list',label: 'Wellness Plans',        desc: 'Create plans with custom activities' },
    { icon: 'fas fa-chart-line',    label: 'Progress Tracking',     desc: 'Monitor daily activity completion' },
    { icon: 'fas fa-file-medical',  label: 'Health Reports',        desc: 'Generate & download PDF reports' },
  ];

  onSubmit(): void {
    if (!this.username() || !this.password()) {
      this.toast.error('Please enter username and password.');
      return;
    }
    this.loading.set(true);
    this.auth.login({ username: this.username(), password: this.password() }).subscribe({
      next: res => {
        this.loading.set(false);
        this.toast.success(`Welcome back, ${res.username}!`);
        this.router.navigate([res.role === 'ADMIN' ? '/admin/dashboard' : '/patient/dashboard']);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Invalid username or password.');
      }
    });
  }

  onRegister(): void {
    if (!this.regReferenceId() || !this.regEmail() || !this.regUsername() ||
        !this.regPassword() || !this.regConfirm()) {
      this.toast.error('All fields are required.');
      return;
    }
    if (this.regPassword() !== this.regConfirm()) {
      this.toast.error('Passwords do not match.');
      return;
    }
    const refId = parseInt(this.regReferenceId(), 10);
    if (isNaN(refId) || refId <= 0) {
      this.toast.error('Patient ID must be a valid number provided by your admin.');
      return;
    }
    this.regLoading.set(true);
    this.auth.register({
      username: this.regUsername(),
      password: this.regPassword(),
      role: 'PATIENT',
      referenceId: refId,
      patientEmail: this.regEmail().trim()
    }).subscribe({
      next: res => {
        this.regLoading.set(false);
        this.toast.success('Account created! Welcome, ' + res.username + '!');
        this.router.navigate(['/patient/dashboard']);
      },
      error: (err) => {
        this.regLoading.set(false);
        const msg = err?.error?.message ?? err?.error ?? 'Registration failed. Please check your details.';
        this.toast.error(msg);
      }
    });
  }
}
