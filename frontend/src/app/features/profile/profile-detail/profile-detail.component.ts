import { Component, inject, signal, Input, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { ProfileResponse } from '../../../core/models/user.model';

@Component({
  selector: 'fl-profile-detail',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './profile-detail.component.html',
  styleUrls: ['../my-profile/my-profile.component.scss'],
})
export class ProfileDetailComponent implements OnInit {
  @Input() userId!: string;

  private readonly user = inject(UserService);
  private readonly router = inject(Router);

  readonly profile = signal<ProfileResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.user.getById(this.userId).subscribe({
      next: (p) => { this.profile.set(p); this.loading.set(false); },
      error: (err) => { this.error.set(err?.error?.message ?? 'Profile not found.'); this.loading.set(false); },
    });
  }

  initials(name?: string): string {
    return (name ?? '').split(' ').map((s) => s[0]).filter(Boolean).slice(0, 2).join('').toUpperCase() || '??';
  }

  message(): void {
    this.router.navigate(['/messages/chat', this.userId]);
  }
}
