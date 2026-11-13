import { Routes } from '@angular/router';
import { connexionGuard } from '../../core/auth/gardes';

/** Module briques (membre 2) : M01, M02, M03, M05. Routes statiques avant /:id (dossier 12.4). */
export const BRIQUES_ROUTES: Routes = [
  {
    path: '',
    title: 'Les briques – DevBrik',
    loadComponent: () => import('./liste/liste-briques.component').then((m) => m.ListeBriquesComponent),
  },
  {
    path: 'nouvelle',
    title: 'Publier une brique – DevBrik',
    canActivate: [connexionGuard],
    loadComponent: () => import('./formulaire/formulaire-brique.component').then((m) => m.FormulaireBriqueComponent),
  },
  {
    path: 'mes-briques',
    title: 'Mes briques – DevBrik',
    canActivate: [connexionGuard],
    loadComponent: () => import('./mes-briques/mes-briques.component').then((m) => m.MesBriquesComponent),
  },
  {
    path: ':id',
    title: 'Fiche de la brique – DevBrik',
    loadComponent: () => import('./fiche/fiche-brique.component').then((m) => m.FicheBriqueComponent),
  },
  {
    path: ':id/modifier',
    title: 'Modifier la brique – DevBrik',
    canActivate: [connexionGuard],
    loadComponent: () => import('./formulaire/formulaire-brique.component').then((m) => m.FormulaireBriqueComponent),
  },
];
