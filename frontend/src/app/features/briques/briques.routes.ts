import { Routes } from '@angular/router';
import { authGuard } from '../../core/auth/auth.guard';

export const BRIQUES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./m01-liste/brique-liste.component').then(m => m.BriqueListeComponent),
  },
  {
    path: 'publier',
    loadComponent: () =>
      import('./m03-publier/brique-formulaire.component').then(m => m.BriqueFormulaireComponent),
    canActivate: [authGuard],
  },
  {
    path: 'mes-briques',
    loadComponent: () =>
      import('./m05-mes-briques/mes-briques.component').then(m => m.MesBriquesComponent),
    canActivate: [authGuard],
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./m02-fiche/brique-fiche.component').then(m => m.BriqueFicheComponent),
  },
  {
    path: ':id/modifier',
    loadComponent: () =>
      import('./m03-publier/brique-formulaire.component').then(m => m.BriqueFormulaireComponent),
    canActivate: [authGuard],
  },
];
