import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthState,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly _state = signal<AuthState | null>(this.loadFromStorage());
  readonly state = this._state.asReadonly();

  readonly currentUser = computed(() => {
    const s = this._state();
    return s ? { id: s.userId, name: s.name, email: s.email, roles: s.roles } : null;
  });
  readonly roles = computed(() => this._state()?.roles ?? []);

  isLoggedIn(): boolean {
    const s = this._state();
    return !!s && s.expiresAt > Date.now();
  }

  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  hasAnyRole(...roles: string[]): boolean {
    const mine = this.roles();
    return roles.some((r) => mine.includes(r));
  }

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, req)
      .pipe(tap((res) => this.persist(res)));
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, req)
      .pipe(tap((res) => this.persist(res)));
  }

  me(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/me`);
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this._state()?.refreshToken;
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken })
      .pipe(tap((res) => this.persist(res)));
  }

  logout(redirect = true): void {
    localStorage.removeItem(environment.tokenStorageKey);
    this._state.set(null);
    if (redirect) this.router.navigate(['/login']);
  }

  private persist(res: AuthResponse): void {
    const state: AuthState = {
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      userId: res.userId,
      email: res.email,
      name: res.name,
      roles: res.roles ?? [],
      expiresAt: Date.now() + res.expiresInSeconds * 1000,
    };
    localStorage.setItem(environment.tokenStorageKey, JSON.stringify(state));
    this._state.set(state);
  }

  private loadFromStorage(): AuthState | null {
    try {
      const raw = localStorage.getItem(environment.tokenStorageKey);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as AuthState;
      if (!parsed?.accessToken || parsed.expiresAt <= Date.now()) {
        localStorage.removeItem(environment.tokenStorageKey);
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }
}
