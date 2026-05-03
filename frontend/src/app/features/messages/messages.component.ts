import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from '../../core/services/message.service';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { Message } from '../../core/models/message.model';
import { ProfileResponse } from '../../core/models/user.model';

@Component({
  selector: 'fl-messages',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.scss',
})
export class MessagesComponent implements OnInit {
  /** Optional route input: /messages/chat/:otherUserId */
  @Input() otherUserId?: string;

  private readonly msg = inject(MessageService);
  private readonly user = inject(UserService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly me = this.auth.currentUser;
  readonly contacts = signal<ProfileResponse[]>([]);
  readonly loadingContacts = signal(true);

  readonly activeId = signal<string | null>(null);
  readonly activeContact = computed(() =>
    this.contacts().find((c) => String(c.userId) === this.activeId()) ?? null,
  );
  readonly messages = signal<Message[]>([]);
  readonly loadingMessages = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    content: ['', [Validators.required]],
  });

  /** Lookup form – start a new chat by email */
  readonly lookup = this.fb.nonNullable.group({ email: ['', [Validators.email]] });
  readonly lookupError = signal<string | null>(null);

  constructor() {
    this.user.list(0, 50).subscribe({
      next: (p) => {
        const meId = String(this.me()?.id ?? '');
        this.contacts.set((p.content ?? []).filter((c) => String(c.userId) !== meId));
        this.loadingContacts.set(false);
      },
      error: () => this.loadingContacts.set(false),
    });
  }

  ngOnInit(): void {
    if (this.otherUserId) this.openChat(this.otherUserId);
  }

  openChat(userId: string | number): void {
    this.activeId.set(String(userId));
    this.messages.set([]);
    this.loadingMessages.set(true);
    this.error.set(null);
    this.msg.conversation(userId).subscribe({
      next: (m) => { this.messages.set(m ?? []); this.loadingMessages.set(false); },
      error: (err) => { this.error.set(err?.error?.message ?? 'Could not load conversation.'); this.loadingMessages.set(false); },
    });
    this.router.navigate(['/messages/chat', String(userId)], { replaceUrl: true });
  }

  send(): void {
    const content = this.form.value.content?.trim();
    const receiverId = this.activeId();
    if (!content || !receiverId) return;
    this.msg.send({ receiverId, content }).subscribe({
      next: (m) => {
        this.messages.update((rows) => [...rows, m]);
        this.form.reset({ content: '' });
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Could not send message.'),
    });
  }

  startChatByEmail(): void {
    this.lookupError.set(null);
    const email = this.lookup.value.email?.trim();
    if (!email) return;
    this.user.lookupByEmail(email).subscribe({
      next: (p) => {
        this.contacts.update((rows) => {
          if (rows.find((r) => r.userId === p.userId)) return rows;
          return [p, ...rows];
        });
        this.openChat(p.userId);
        this.lookup.reset({ email: '' });
      },
      error: () => this.lookupError.set('No user found with that email.'),
    });
  }

  initials(name: string): string {
    return (name ?? '').split(' ').map((s) => s[0]).filter(Boolean).slice(0, 2).join('').toUpperCase() || '??';
  }

  isMine(m: Message): boolean { return String(m.senderId) === String(this.me()?.id); }
}
