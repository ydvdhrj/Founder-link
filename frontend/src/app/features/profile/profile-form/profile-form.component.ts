import { Component, inject, signal, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProfileResponse } from '../../../core/models/user.model';

@Component({
  selector: 'fl-profile-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile-form.component.html',
  styleUrl: './profile-form.component.scss',
})
export class ProfileFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly mode = signal<'create' | 'edit'>('create');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email]],
    skills: [''],
    experience: [''],
    bio: [''],
    portfolioLinks: this.fb.array<FormControl<string>>([]),
  });

  get linksArr(): FormArray<FormControl<string>> { return this.form.controls.portfolioLinks; }

  ngOnInit(): void {
    const mode = this.route.snapshot.data['mode'] as 'create' | 'edit' | undefined;
    this.mode.set(mode ?? 'create');
    const me = this.auth.currentUser();
    if (me) {
      this.form.patchValue({ name: me.name, email: me.email });
    }
    if (this.mode() === 'edit' && me) {
      this.userService.getById(me.id).subscribe({
        next: (p) => this.fillFrom(p),
        error: () => {},
      });
    }
  }

  addLink(value = ''): void {
    this.linksArr.push(this.fb.nonNullable.control(value, { validators: [Validators.maxLength(2048)] }));
  }
  removeLink(i: number): void { this.linksArr.removeAt(i); }

  private fillFrom(p: ProfileResponse): void {
    this.form.patchValue({
      name: p.name, email: p.email,
      skills: p.skills ?? '', experience: p.experience ?? '', bio: p.bio ?? '',
    });
    this.linksArr.clear();
    (p.portfolioLinks ?? []).forEach((l) => this.addLink(l));
  }

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    const payload = this.form.getRawValue();
    const me = this.auth.currentUser();
    const obs =
      this.mode() === 'edit' && me
        ? this.userService.update(me.id, payload)
        : this.userService.create(payload);
    obs.subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set('Profile saved successfully.');
        setTimeout(() => this.router.navigate(['/profile']), 600);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Could not save profile.');
      },
    });
  }
}
