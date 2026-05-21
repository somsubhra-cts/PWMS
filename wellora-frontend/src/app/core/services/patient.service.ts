import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Patient, PatientRequest } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/patients`;

  getAll(): Observable<Patient[]> {
    return this.http.get<Patient[]>(this.base);
  }

  getById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.base}/${id}`);
  }

  search(name: string): Observable<Patient> {
    return this.http.get<Patient>(`${this.base}/search`, { params: { name } });
  }

  create(req: PatientRequest): Observable<Patient> {
    return this.http.post<Patient>(this.base, req);
  }

  update(id: number, req: PatientRequest): Observable<Patient> {
    return this.http.put<Patient>(`${this.base}/${id}`, req);
  }

  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.base}/${id}`);
  }
}
