import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { ProfileResponse } from '../../../core/models/user.model';

@Component({
  selector: 'fl-directory',
  standalone: true,
  imports: [],
  templateUrl: './directory.component.html',
  styleUrl: './directory.component.scss',
})
export class DirectoryComponent {
  private readonly user = inject(UserService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<ProfileResponse[]>([]);
  readonly filter = signal('');

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true);
    this.user.list(0, 50).subscribe({
      next: (p) => { this.rows.set(p.content ?? []); this.loading.set(false); },
      error: (err) => { this.error.set(err?.error?.message ?? 'Could not load directory.'); this.loading.set(false); },
    });
  }

  filtered(): ProfileResponse[] {
    const f = this.filter().toLowerCase().trim();
    if (!f) return this.rows();
    return this.rows().filter((r) =>
      (r.name + ' ' + (r.skills ?? '') + ' ' + (r.bio ?? '')).toLowerCase().includes(f),
    );
  }

  initials(name: string): string {
    return name.split(' ').map((s) => s[0]).filter(Boolean).slice(0, 2).join('').toUpperCase();
  }

  open(r: ProfileResponse): void { this.router.navigate(['/directory', r.userId]); }
}
