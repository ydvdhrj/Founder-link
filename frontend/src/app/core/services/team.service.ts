import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InviteMemberRequest, TeamMember } from '../models/team.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/teams`;

  invite(req: InviteMemberRequest): Observable<TeamMember> {
    return this.http.post<TeamMember>(`${this.baseUrl}/invite`, req);
  }

  join(inviteId: string): Observable<TeamMember> {
    return this.http.post<TeamMember>(`${this.baseUrl}/join/${inviteId}`, {});
  }

  byStartup(startupId: string): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.baseUrl}/startup/${startupId}`);
  }
}
