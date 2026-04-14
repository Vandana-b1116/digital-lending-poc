import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoanApplication } from '../../store/applications/applications.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly base = `${environment.applicationServiceUrl}/api/applications`;

  constructor(private http: HttpClient) {}

  getApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(this.base);
  }

  getApplication(id: number): Observable<LoanApplication> {
    return this.http.get<LoanApplication>(`${this.base}/${id}`);
  }

  createApplication(payload: Partial<LoanApplication>): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(this.base, payload);
  }

  updateStatus(id: number, status: string): Observable<LoanApplication> {
    return this.http.patch<LoanApplication>(`${this.base}/${id}/status`, { status });
  }

  submitToUnderwriting(id: number): Observable<unknown> {
    return this.http.post(`${this.base}/${id}/submit-to-underwriting`, {});
  }
}
