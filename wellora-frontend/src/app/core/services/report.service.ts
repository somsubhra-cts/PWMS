import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Report, ReportPreview, GenerateReportRequest } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/reports`;

  preview(patientId: number, planId: number): Observable<ReportPreview> {
    return this.http.get<ReportPreview>(`${this.base}/preview/${patientId}/${planId}`);
  }

  generate(patientId: number, planId: number, req: GenerateReportRequest): Observable<Report> {
    return this.http.post<Report>(`${this.base}/generate/${patientId}/${planId}`, req);
  }

  getAllByPatientAdmin(patientId: number): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.base}/admin/patient/${patientId}`);
  }

  getPublishedByPatient(patientId: number): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.base}/patient/${patientId}`);
  }

  getById(reportId: number): Observable<Report> {
    return this.http.get<Report>(`${this.base}/${reportId}`);
  }

  getByDateRange(patientId: number, from: string, to: string): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.base}/patient/${patientId}/range`, {
      params: { from, to }
    });
  }

  downloadPdf(reportId: number): Observable<Blob> {
    return this.http.get(`${this.base}/download/${reportId}`, {
      responseType: 'blob'
    });
  }
}
