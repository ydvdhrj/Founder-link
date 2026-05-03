import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateInvestmentRequest,
  Investment,
  InvestmentStatus,
} from '../models/investment.model';

@Injectable({ providedIn: 'root' })
export class InvestmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/investments`;

  create(req: CreateInvestmentRequest): Observable<Investment> {
    return this.http.post<Investment>(this.baseUrl, req);
  }

  byStartup(startupId: string): Observable<Investment[]> {
    return this.http.get<Investment[]>(`${this.baseUrl}/startup/${startupId}`);
  }

  mine(): Observable<Investment[]> {
    return this.http.get<Investment[]>(`${this.baseUrl}/investor`);
  }

  updateStatus(id: string, status: InvestmentStatus): Observable<Investment> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Investment>(`${this.baseUrl}/${id}/status`, null, { params });
  }
}
