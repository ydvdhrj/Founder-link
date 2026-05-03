import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Message, SendMessageRequest } from '../models/message.model';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/messages`;

  send(req: SendMessageRequest): Observable<Message> {
    return this.http.post<Message>(this.baseUrl, req);
  }

  conversation(otherUserId: string | number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.baseUrl}/conversation/${otherUserId}`);
  }
}
