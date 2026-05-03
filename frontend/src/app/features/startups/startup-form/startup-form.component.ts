import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { StartupStage } from '../../../core/models/startup.model';

@Component({
  selector: 'fl-startup-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './startup-form.component.html',
  styleUrls: ['../../profile/profile-form/profile-form.component.scss'],
})
export class StartupFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly startup = inject(StartupService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly stages: { value: StartupStage; label: string }[] = [
    { value: 'IDEA', label: 'Idea' },
    { value: 'MVP', label: 'MVP' },
    { value: 'EARLY_TRACTION', label: 'Early Traction' },
    { value: 'SCALING', label: 'Scaling' },
  ];

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    industry: [''],
    stage: ['IDEA' as StartupStage, Validators.required],
    fundingGoal: [0, [Validators.required, Validators.min(0)]],
  });

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    this.startup.create(this.form.getRawValue()).subscribe({
      next: (created) => {
        this.loading.set(false);
        this.router.navigate(['/startups', created.id]);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Could not create startup.');
      },
    });
  }
}
