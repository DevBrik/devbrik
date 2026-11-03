import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const connexionGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estConnecte()
    ? true
    : router.createUrlTree(['/compte/connexion'], { queryParams: { retour: state.url } });
};

export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.estConnecte()) {
    return router.createUrlTree(['/compte/connexion'], { queryParams: { retour: state.url } });
  }
  return auth.estAdmin() ? true : router.createUrlTree(['/acces-refuse']);
};