import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const RoleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const allowedRoles = route.data['roles'] as string[];

  if (!user?.roles) {
    router.navigate(['/sign-in']);
    return false;
  }

  const hasRole = user.roles.some((role: string) => allowedRoles.includes(role));
  if (!hasRole) {
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
};
