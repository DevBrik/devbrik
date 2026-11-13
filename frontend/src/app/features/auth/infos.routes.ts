import { Routes } from '@angular/router';

/** Pages statiques M13 (membre 5). */
export const INFOS_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'a-propos' },
  {
    path: 'conditions',
    title: "Conditions d'utilisation – DevBrik",
    loadComponent: () => import('./pages/conditions/conditions.component').then((m) => m.ConditionsComponent),
  },
  {
    path: 'confidentialite',
    title: 'Politique de confidentialité – DevBrik',
    loadComponent: () => import('./pages/confidentialite/confidentialite.component').then((m) => m.ConfidentialiteComponent),
  },
  {
    path: 'a-propos',
    title: 'À propos – DevBrik',
    loadComponent: () => import('./pages/a-propos/a-propos.component').then((m) => m.AProposComponent),
  },
];
