import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth.service';
import { StartupResponseDTO } from '../../../core/models/startup.model';
import { Investment, InvestmentStatus } from '../../../core/models/investment.model';
import { TeamMember } from '../../../core/models/team.model';

@Component({
  selector: 'fl-startup-detail',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './startup-detail.component.html',
  styleUrl: './startup-detail.component.scss',
})
export class StartupDetailComponent implements OnInit {
  @Input() id!: string;

  private readonly startupService = inject(StartupService);
  private readonly investService = inject(InvestmentService);
  private readonly teamService = inject(TeamService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly data = signal<StartupResponseDTO | null>(null);
  readonly investments = signal<Investment[]>([]);
  readonly members = signal<TeamMember[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly investError = signal<string | null>(null);
  readonly investSuccess = signal<string | null>(null);

  readonly isInvestor = computed(() => this.auth.hasRole('ROLE_INVESTOR'));
  readonly isOwner = computed(() => {
    const me = String(this.auth.currentUser()?.id ?? '');
    return me && me === String(this.data()?.startup?.founderId);
  });

  readonly investForm = this.fb.nonNullable.group({
    amount: [1000, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void { this.load(); }

  private load(): void {
    this.startupService.getById(this.id).subscribe({
      next: (dto) => { this.data.set(dto); this.loading.set(false); },
      error: (err) => { this.error.set(err?.error?.message ?? 'Startup not found.'); this.loading.set(false); },
    });
    this.investService.byStartup(this.id).subscribe({
      next: (rows) => this.investments.set(rows ?? []),
      error: () => this.investments.set([]),
    });
    this.teamService.byStartup(this.id).subscribe({
      next: (rows) => this.members.set(rows ?? []),
      error: () => this.members.set([]),
    });
  }

  invest(): void {
    this.investError.set(null);
    this.investSuccess.set(null);
    if (this.investForm.invalid) return;
    this.investService
      .create({ startupId: this.id, amount: this.investForm.getRawValue().amount })
      .subscribe({
        next: (inv) => {
          this.investments.update((rows) => [inv, ...rows]);
          this.investSuccess.set('Investment submitted! Awaiting founder approval.');
          this.investForm.reset({ amount: 1000 });
        },
        error: (err) => this.investError.set(err?.error?.message ?? 'Could not submit investment.'),
      });
  }

  updateStatus(id: string, status: InvestmentStatus): void {
    this.investService.updateStatus(id, status).subscribe({
      next: (updated) => {
        this.investments.update((rows) => rows.map((r) => (r.id === id ? updated : r)));
      },
    });
  }

  fmt(n?: number): string {
    if (!n) return '$0';
    if (n >= 1_000_000) return `$${(n / 1_000_000).toFixed(1)}M`;
    if (n >= 1_000) return `$${(n / 1_000).toFixed(1)}K`;
    return `$${n}`;
  }

  statusClass(s: InvestmentStatus): string {
    switch (s) {
      case 'APPROVED': case 'COMPLETED': return 'success';
      case 'REJECTED': return 'danger';
      default: return 'warn';
    }
  }

  messageFounder(): void {
    const founderId = this.data()?.startup?.founderId;
    if (founderId) this.router.navigate(['/messages/chat', founderId]);
  }

  inviteToTeam(): void {
    this.router.navigate(['/teams/invite'], { queryParams: { startupId: this.id } });
  }
}
