import { Routes } from '@angular/router';

/** Module développeurs (membre 4) : classement (P2) et profil public M09. Classement avant /:id. */
export const DEVELOPPEURS_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'classement' },
  {
    path: 'classement',
    title: 'Classement des développeurs – DevBrik',
    loadComponent: () => import('./classement/classement.component').then((m) => m.ClassementComponent),
  },
  {
    path: ':id',
    title: 'Profil du développeur – DevBrik',
    loadComponent: () => import('./profil/profil-public.component').then((m) => m.ProfilPublicComponent),
  },
];
