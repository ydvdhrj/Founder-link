import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup } from '../../../core/models/startup.model';

@Component({
  selector: 'fl-startup-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './startup-list.component.html',
  styleUrl: './startup-list.component.scss',
})
export class StartupListComponent {
  private readonly startup = inject(StartupService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly rows = signal<Startup[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly filter = signal('');
  readonly stage = signal<string>('');

  isFounder() { return this.auth.hasRole('ROLE_FOUNDER'); }

  constructor() {
    this.startup.list().subscribe({
      next: (r) => { this.rows.set(r ?? []); this.loading.set(false); },
      error: (err) => { this.error.set(err?.error?.message ?? 'Could not load startups.'); this.loading.set(false); },
    });
  }

  filtered(): Startup[] {
    const f = this.filter().toLowerCase().trim();
    const st = this.stage();
    return this.rows().filter((r) => {
      const matchesText =
        !f ||
        (r.name + ' ' + (r.description ?? '') + ' ' + (r.industry ?? '')).toLowerCase().includes(f);
      const matchesStage = !st || r.stage === st;
      return matchesText && matchesStage;
    });
  }

  fmt(n: number): string {
    if (!n) return '$0';
    if (n >= 1_000_000) return `$${(n / 1_000_000).toFixed(1)}M`;
    if (n >= 1_000) return `$${(n / 1_000).toFixed(1)}K`;
    return `$${n}`;
  }

  stageClass(stage: string): string {
    switch (stage) {
      case 'IDEA': return 'info';
      case 'MVP': return 'warn';
      case 'EARLY_TRACTION': return 'success';
      case 'SCALING': return '';
      default: return '';
    }
  }

  open(s: Startup): void { this.router.navigate(['/startups', s.id]); }
}
