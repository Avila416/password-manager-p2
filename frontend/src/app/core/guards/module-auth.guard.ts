import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const moduleAuthGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('pm_token');
  return token ? true : router.createUrlTree(['/auth-required']);
};
