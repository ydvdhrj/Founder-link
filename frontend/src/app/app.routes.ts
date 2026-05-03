import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'signup',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/signup/signup.component').then((m) => m.SignupComponent),
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },

      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/my-profile/my-profile.component').then((m) => m.MyProfileComponent),
      },
      {
        path: 'profile/create',
        data: { mode: 'create' },
        loadComponent: () =>
          import('./features/profile/profile-form/profile-form.component').then((m) => m.ProfileFormComponent),
      },
      {
        path: 'profile/edit',
        data: { mode: 'edit' },
        loadComponent: () =>
          import('./features/profile/profile-form/profile-form.component').then((m) => m.ProfileFormComponent),
      },
      {
        path: 'directory',
        loadComponent: () =>
          import('./features/profile/directory/directory.component').then((m) => m.DirectoryComponent),
      },
      {
        path: 'directory/:userId',
        loadComponent: () =>
          import('./features/profile/profile-detail/profile-detail.component').then((m) => m.ProfileDetailComponent),
      },

      {
        path: 'startups',
        loadComponent: () =>
          import('./features/startups/startup-list/startup-list.component').then((m) => m.StartupListComponent),
      },
      {
        path: 'startups/new',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_FOUNDER', 'ROLE_ADMIN'] },
        loadComponent: () =>
          import('./features/startups/startup-form/startup-form.component').then((m) => m.StartupFormComponent),
      },
      {
        path: 'startups/:id',
        loadComponent: () =>
          import('./features/startups/startup-detail/startup-detail.component').then((m) => m.StartupDetailComponent),
      },

      {
        path: 'investments',
        loadComponent: () =>
          import('./features/investments/investment-list.component').then((m) => m.InvestmentListComponent),
      },

      {
        path: 'teams',
        pathMatch: 'full',
        redirectTo: 'teams/invite',
      },
      {
        path: 'teams/invite',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_FOUNDER', 'ROLE_ADMIN'] },
        loadComponent: () =>
          import('./features/teams/team-invite.component').then((m) => m.TeamInviteComponent),
      },

      {
        path: 'messages',
        loadComponent: () =>
          import('./features/messages/messages.component').then((m) => m.MessagesComponent),
      },
      {
        path: 'messages/chat/:otherUserId',
        loadComponent: () =>
          import('./features/messages/messages.component').then((m) => m.MessagesComponent),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
