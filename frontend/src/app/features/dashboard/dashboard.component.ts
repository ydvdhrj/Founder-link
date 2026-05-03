import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StartupService } from '../../core/services/startup.service';
import { InvestmentService } from '../../core/services/investment.service';
import { Startup } from '../../core/models/startup.model';
import { Investment } from '../../core/models/investment.model';

@Component({
  selector: 'fl-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly startups = inject(StartupService);
  private readonly investments = inject(InvestmentService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly isFounder = computed(() => this.auth.hasRole('ROLE_FOUNDER'));
  readonly isInvestor = computed(() => this.auth.hasRole('ROLE_INVESTOR'));

  readonly allStartups = signal<Startup[]>([]);
  readonly myStartups = computed(() => {
    const me = String(this.user()?.id ?? '');
    return this.allStartups().filter((s) => String(s.founderId) === me);
  });
  readonly myInvestments = signal<Investment[]>([]);

  readonly totalFunding = computed(() =>
    this.myStartups().reduce((sum, s) => sum + (s.fundingGoal ?? 0), 0),
  );
  readonly totalInvested = computed(() =>
    this.myInvestments().reduce((sum, i) => sum + (i.amount ?? 0), 0),
  );

  constructor() {
    this.startups.list().subscribe({
      next: (rows) => this.allStartups.set(rows ?? []),
      error: () => this.allStartups.set([]),
    });
    if (this.auth.hasRole('ROLE_INVESTOR')) {
      this.investments.mine().subscribe({
        next: (rows) => this.myInvestments.set(rows ?? []),
        error: () => this.myInvestments.set([]),
      });
    }
  }

  openStartup(id: string) { this.router.navigate(['/startups', id]); }

  fmtMoney(n: number): string {
    if (!n) return '$0';
    if (n >= 1_000_000) return `$${(n / 1_000_000).toFixed(1)}M`;
    if (n >= 1_000) return `$${(n / 1_000).toFixed(1)}K`;
    return `$${n}`;
  }
}
