import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/gardes';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'briques' },
  {
    path: 'briques',
    loadChildren: () => import('./features/briques/briques.routes').then((m) => m.BRIQUES_ROUTES),
  },
  {
    path: 'demandes',
    loadChildren: () => import('./features/demandes/demandes.routes').then((m) => m.DEMANDES_ROUTES),
  },
  {
    path: 'developpeurs',
    loadChildren: () => import('./features/demandes/profil.routes').then((m) => m.PROFIL_ROUTES),
  },
  {
    path: 'compte',
    loadChildren: () => import('./features/auth/compte.routes').then((m) => m.COMPTE_ROUTES),
  },
  {
    path: 'infos',
    loadChildren: () => import('./features/auth/infos.routes').then((m) => m.INFOS_ROUTES),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./features/avis/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  { path: 'acces-refuse', loadComponent: () => import('./shared/pages/pages').then((m) => m.AccesRefuse) },
  { path: '**', loadComponent: () => import('./shared/pages/pages').then((m) => m.PageIntrouvable) },
];