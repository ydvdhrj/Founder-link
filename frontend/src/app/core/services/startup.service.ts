import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateStartupRequest,
  Startup,
  StartupResponseDTO,
} from '../models/startup.model';

@Injectable({ providedIn: 'root' })
export class StartupService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/startups`;

  list(): Observable<Startup[]> {
    return this.http.get<Startup[]>(this.baseUrl);
  }

  getById(id: string): Observable<StartupResponseDTO> {
    return this.http.get<StartupResponseDTO>(`${this.baseUrl}/${id}`);
  }

  create(req: CreateStartupRequest): Observable<Startup> {
    return this.http.post<Startup>(this.baseUrl, req);
  }
}
