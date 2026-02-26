import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { readAuthToken } from '../auth/token.util';

@Injectable({ providedIn: 'root' })
export class ModuleAuthGuard implements CanActivate {
  constructor(private readonly router: Router) {}

  canActivate(): boolean | UrlTree {
    const token = readAuthToken();
    if (token) {
      return true;
    }
    return this.router.createUrlTree(['/auth-required']);
  }
}
