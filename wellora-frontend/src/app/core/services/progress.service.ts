import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProgressRecord, ProgressSummary, StatusUpdateRequest } from '../models/progress.model';

@Injectable({ providedIn: 'root' })
export class ProgressService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/progress`;

  init(patientId: number, planId: number): Observable<string> {
    return this.http.post(`${this.base}/init`, null, {
      params: { patientId, planId },
      responseType: 'text'
    });
  }

  seed(patientId: number, planId: number): Observable<ProgressRecord[]> {
    return this.http.post<ProgressRecord[]>(`${this.base}/seed`, null, {
      params: { patientId, planId }
    });
  }

  updateStatus(patientId: number, req: StatusUpdateRequest): Observable<ProgressRecord> {
    return this.http.patch<ProgressRecord>(`${this.base}/update/${patientId}`, req);
  }

  getByPatient(patientId: number): Observable<ProgressRecord[]> {
    return this.http.get<ProgressRecord[]>(`${this.base}/patient/${patientId}`);
  }

  getByPatientAndPlan(patientId: number, planId: number): Observable<ProgressRecord[]> {
    return this.http.get<ProgressRecord[]>(`${this.base}/patient/${patientId}/plan/${planId}`);
  }

  getByPatientAndDate(patientId: number, date: string): Observable<ProgressRecord[]> {
    return this.http.get<ProgressRecord[]>(`${this.base}/patient/${patientId}/date/${date}`);
  }

  getByPatientPlanDate(patientId: number, planId: number, date: string): Observable<ProgressRecord[]> {
    return this.http.get<ProgressRecord[]>(`${this.base}/patient/${patientId}/plan/${planId}/date/${date}`);
  }

  getSummary(patientId: number, planId: number): Observable<ProgressSummary> {
    return this.http.get<ProgressSummary>(`${this.base}/summary/${patientId}/plan/${planId}`);
  }
}
