import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateProfileRequest,
  PageResponse,
  ProfileResponse,
  UpdateProfileRequest,
} from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  list(page = 0, size = 20): Observable<PageResponse<ProfileResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<ProfileResponse>>(this.baseUrl, { params });
  }

  getById(userId: number | string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.baseUrl}/${userId}`);
  }

  lookupByEmail(email: string): Observable<ProfileResponse> {
    const params = new HttpParams().set('email', email);
    return this.http.get<ProfileResponse>(`${this.baseUrl}/lookup`, { params });
  }

  create(req: CreateProfileRequest): Observable<ProfileResponse> {
    return this.http.post<ProfileResponse>(this.baseUrl, req);
  }

  update(userId: number | string, req: UpdateProfileRequest): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.baseUrl}/${userId}`, req);
  }
}
