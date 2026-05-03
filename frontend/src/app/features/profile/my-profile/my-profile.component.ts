import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { ProfileResponse } from '../../../core/models/user.model';

@Component({
  selector: 'fl-my-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './my-profile.component.html',
  styleUrl: './my-profile.component.scss',
})
export class MyProfileComponent {
  private readonly auth = inject(AuthService);
  private readonly user = inject(UserService);
  private readonly router = inject(Router);

  readonly me = this.auth.currentUser;
  readonly profile = signal<ProfileResponse | null>(null);
  readonly notFound = signal(false);
  readonly loading = signal(true);

  constructor() {
    const id = this.auth.currentUser()?.id;
    if (id) {
      this.user.getById(id).subscribe({
        next: (p) => { this.profile.set(p); this.loading.set(false); },
        error: () => { this.notFound.set(true); this.loading.set(false); },
      });
    } else {
      this.loading.set(false);
    }
  }

  initials(name?: string): string {
    return (name ?? '').split(' ').map((s) => s[0]).filter(Boolean).slice(0, 2).join('').toUpperCase() || '??';
  }
}
