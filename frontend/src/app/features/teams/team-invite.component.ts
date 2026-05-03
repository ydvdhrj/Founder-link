import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TeamService } from '../../core/services/team.service';
import { StartupService } from '../../core/services/startup.service';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { TeamRole } from '../../core/models/team.model';
import { Startup } from '../../core/models/startup.model';

@Component({
  selector: 'fl-team-invite',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './team-invite.component.html',
  styleUrl: './team-invite.component.scss',
})
export class TeamInviteComponent {
  private readonly fb = inject(FormBuilder);
  private readonly teamService = inject(TeamService);
  private readonly startupService = inject(StartupService);
  private readonly userService = inject(UserService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly myStartups = signal<Startup[]>([]);

  readonly roles: { value: TeamRole; label: string }[] = [
    { value: 'CTO', label: 'CTO' },
    { value: 'CPO', label: 'Chief Product Officer' },
    { value: 'MARKETING_HEAD', label: 'Marketing Head' },
    { value: 'ENGINEERING_LEAD', label: 'Engineering Lead' },
  ];

  readonly form = this.fb.nonNullable.group({
    startupId: ['', Validators.required],
    inviteeEmail: ['', [Validators.required, Validators.email]],
    role: ['CTO' as TeamRole, Validators.required],
  });

  constructor() {
    this.startupService.list().subscribe({
      next: (rows) => {
        const me = String(this.auth.currentUser()?.id ?? '');
        const mine = (rows ?? []).filter((s) => String(s.founderId) === me);
        this.myStartups.set(mine);
        const preset = this.route.snapshot.queryParamMap.get('startupId');
        if (preset) this.form.controls.startupId.setValue(preset);
        else if (mine.length === 1) this.form.controls.startupId.setValue(mine[0].id);
      },
      error: () => this.myStartups.set([]),
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);
    const { inviteeEmail, startupId, role } = this.form.getRawValue();
    this.userService.lookupByEmail(inviteeEmail).subscribe({
      next: (profile) => {
        this.teamService
          .invite({ startupId, invitedUserId: String(profile.userId), role })
          .subscribe({
            next: () => {
              this.loading.set(false);
              this.success.set(`Invitation sent to ${profile.name}.`);
              this.form.reset({ startupId, inviteeEmail: '', role });
            },
            error: (err) => {
              this.loading.set(false);
              this.error.set(err?.error?.message ?? 'Could not send invite.');
            },
          });
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No user found with that email. Ask them to sign up first.');
      },
    });
  }
}
