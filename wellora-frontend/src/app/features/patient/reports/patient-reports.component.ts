import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ReportService } from '../../../core/services/report.service';
import { ToastService } from '../../../core/services/toast.service';
import { Report } from '../../../core/models/report.model';

@Component({
  selector: 'app-patient-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './patient-reports.component.html',
  styleUrls: ['./patient-reports.component.css']
})
export class PatientReportsComponent implements OnInit {
  private auth      = inject(AuthService);
  private reportSvc = inject(ReportService);
  private toast     = inject(ToastService);

  reports   = signal<Report[]>([]);
  loading   = signal(true);
  selected  = signal<Report | null>(null);
  downloading = signal<number | null>(null);

  get patientId() { return this.auth.referenceId()!; }

  totalReports  = computed(() => this.reports().length);
  latestReport  = computed<Report | null>(() => this.reports()[0] ?? null);

  ngOnInit(): void {
    this.reportSvc.getPublishedByPatient(this.patientId).subscribe({
      next:  r => { this.reports.set(r); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        // 404 = no reports yet â€” show empty state silently, not an error
        if (err?.status !== 404) {
          this.toast.error('Failed to load reports.');
        }
      }
    });
  }

  open(r: Report): void { this.selected.set(r); }
  close(): void { this.selected.set(null); }

  download(r: Report): void {
    this.downloading.set(r.reportId);
    this.reportSvc.downloadPdf(r.reportId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href    = url;
        a.download = `report_${r.reportId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.downloading.set(null);
      },
      error: () => { this.toast.error('Download failed.'); this.downloading.set(null); }
    });
  }

  statusColor(s: string): string {
    return s === 'PUBLISHED' ? 'var(--success)' : s === 'PENDING' ? 'var(--warning)' : 'var(--text-muted)';
  }

  ringOffset(pct: number): number {
    const r = 30; const c = 2 * Math.PI * r;
    return c - (pct / 100) * c;
  }
  circumference = 2 * Math.PI * 30;
}
