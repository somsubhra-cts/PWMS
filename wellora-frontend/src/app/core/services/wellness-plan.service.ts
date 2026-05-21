import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  WellnessPlan, CreateWellnessPlanRequest,
  ActivityDTO, PlanAssignment
} from '../models/wellness-plan.model';

@Injectable({ providedIn: 'root' })
export class WellnessPlanService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/plans`;

  getAll(): Observable<WellnessPlan[]> {
    return this.http.get<WellnessPlan[]>(this.base);
  }

  getById(id: number): Observable<WellnessPlan> {
    return this.http.get<WellnessPlan>(`${this.base}/${id}`);
  }

  create(req: CreateWellnessPlanRequest): Observable<WellnessPlan> {
    return this.http.post<WellnessPlan>(this.base, req);
  }

  update(id: number, req: CreateWellnessPlanRequest): Observable<WellnessPlan> {
    return this.http.put<WellnessPlan>(`${this.base}/${id}`, req);
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' });
  }

  assign(patientId: number, planId: number): Observable<string> {
    return this.http.post(`${this.base}/assign`, null, {
      params: { patientId, planId },
      responseType: 'text'
    });
  }

  getActivities(planId: number): Observable<ActivityDTO[]> {
    return this.http.get<ActivityDTO[]>(`${this.base}/${planId}/activities`);
  }

  getAssignmentsByPatient(patientId: number): Observable<PlanAssignment[]> {
    return this.http.get<PlanAssignment[]>(`${this.base}/assignments/patient/${patientId}`);
  }

  getAssignmentsByPlan(planId: number): Observable<PlanAssignment[]> {
    return this.http.get<PlanAssignment[]>(`${this.base}/assignments/plan/${planId}`);
  }
}
