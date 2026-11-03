import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const jetonInterceptor: HttpInterceptorFn = (req, next) => {
  // Seules les routes de l'API reçoivent le jeton ; jamais celles de /api/auth/.
  if (!req.url.startsWith('/api') || req.url.startsWith('/api/auth/')) {
    return next(req);
  }

  const auth = inject(AuthService);
  const router = inject(Router);
  const jeton = auth.jeton();
  const requete = jeton ? req.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } }) : req;

  return next(requete).pipe(
    catchError((erreur: unknown) => {
      if (erreur instanceof HttpErrorResponse && erreur.status === 401 && jeton) {
        // Jeton invalide ou expiré : on le supprime et on réessaie sans jeton.
        auth.deconnexion();
        return next(req).pipe(
          catchError((erreur2: unknown) => {
            if (erreur2 instanceof HttpErrorResponse && erreur2.status === 401) {
              // Route protégée : retour à la connexion.
              void router.navigate(['/compte/connexion'], { queryParams: { retour: router.url } });
            }
            return throwError(() => erreur2);
          }),
        );
      }
      return throwError(() => erreur);
    }),
  );
};