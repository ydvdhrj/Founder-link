import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RoleName } from '../../../core/models/auth.model';

@Component({
  selector: 'fl-signup',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrls: ['../auth-shell.scss'],
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly roleOptions: { value: RoleName; label: string; hint: string }[] = [
    { value: 'ROLE_FOUNDER', label: 'Founder', hint: 'Build a startup' },
    { value: 'ROLE_INVESTOR', label: 'Investor', hint: 'Back great startups' },
    { value: 'ROLE_COFOUNDER', label: 'Co-founder', hint: 'Join a team' },
  ];

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['ROLE_FOUNDER' as RoleName, Validators.required],
  });

  selectRole(role: RoleName): void {
    this.form.controls.role.setValue(role);
  }

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/profile/create']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Sign up failed. Please try again.');
      },
    });
  }
}
