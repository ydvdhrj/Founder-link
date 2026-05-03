import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  path: string;
  roles?: string[];
}

@Component({
  selector: 'fl-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly roleLabel = computed(() => {
    const roles = this.auth.roles();
    if (roles.includes('ROLE_ADMIN')) return 'Admin';
    if (roles.includes('ROLE_FOUNDER')) return 'Founder';
    if (roles.includes('ROLE_INVESTOR')) return 'Investor';
    if (roles.includes('ROLE_COFOUNDER')) return 'Co-founder';
    return 'Member';
  });
  readonly initials = computed(() => {
    const name = this.user()?.name ?? 'U';
    return name
      .split(' ')
      .map((p) => p[0])
      .filter(Boolean)
      .slice(0, 2)
      .join('')
      .toUpperCase();
  });

  readonly sidebarOpen = signal(true);
  toggleSidebar() { this.sidebarOpen.update((v) => !v); }

  readonly nav: NavItem[] = [
    { label: 'Dashboard',   icon: '⌂', path: '/dashboard' },
    { label: 'Profile',     icon: '☺', path: '/profile' },
    { label: 'Directory',   icon: '≡', path: '/directory' },
    { label: 'Startups',    icon: '★', path: '/startups' },
    { label: 'Investments', icon: '$', path: '/investments' },
    { label: 'Team',        icon: '⚑', path: '/teams' },
    { label: 'Messages',    icon: '✉', path: '/messages' },
  ];

  navFor(): NavItem[] {
    const roles = this.auth.roles();
    return this.nav.filter((item) => !item.roles || item.roles.some((r) => roles.includes(r)));
  }

  logout(): void {
    this.auth.logout();
  }
}
