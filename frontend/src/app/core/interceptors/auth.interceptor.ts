import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Attaches the Bearer access token to every outgoing request that targets our API,
 * unless the request is the login/register endpoint.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.state()?.accessToken;
  const isAuthRoute = req.url.includes('/auth/login') || req.url.includes('/auth/register');

  if (token && !isAuthRoute) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }
  return next(req);
};
