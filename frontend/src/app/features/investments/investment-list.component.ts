import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../core/services/investment.service';
import { AuthService } from '../../core/services/auth.service';
import { Investment, InvestmentStatus } from '../../core/models/investment.model';

@Component({
  selector: 'fl-investment-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './investment-list.component.html',
  styleUrl: './investment-list.component.scss',
})
export class InvestmentListComponent {
  private readonly investService = inject(InvestmentService);
  private readonly auth = inject(AuthService);

  readonly isInvestor = computed(() => this.auth.hasRole('ROLE_INVESTOR'));
  readonly rows = signal<Investment[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly filter = signal<InvestmentStatus | ''>('');

  readonly totalAmount = computed(() => this.rows().reduce((s, i) => s + (i.amount ?? 0), 0));
  readonly pendingCount = computed(() => this.rows().filter((r) => r.status === 'PENDING').length);

  constructor() {
    if (this.auth.hasRole('ROLE_INVESTOR')) {
      this.investService.mine().subscribe({
        next: (rows) => { this.rows.set(rows ?? []); this.loading.set(false); },
        error: (err) => { this.error.set(err?.error?.message ?? 'Could not load investments.'); this.loading.set(false); },
      });
    } else {
      this.loading.set(false);
    }
  }

  filtered(): Investment[] {
    const f = this.filter();
    return f ? this.rows().filter((r) => r.status === f) : this.rows();
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
}
