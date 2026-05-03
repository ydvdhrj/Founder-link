import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Route data: { roles: ['ROLE_FOUNDER', 'ROLE_INVESTOR'] }
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const required = (route.data?.['roles'] as string[] | undefined) ?? [];
  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }
  if (required.length === 0 || auth.hasAnyRole(...required)) return true;
  router.navigate(['/dashboard']);
  return false;
};
