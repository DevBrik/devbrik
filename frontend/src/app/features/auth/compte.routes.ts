import { Routes } from '@angular/router';
import { connexionGuard } from '../../core/auth/gardes';

/** Module comptes (membre 5) : M04, M10. */
export const COMPTE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'connexion' },
  {
    path: 'connexion',
    title: 'Connexion – DevBrik',
    loadComponent: () => import('./connexion/connexion.component').then((m) => m.ConnexionComponent),
  },
  {
    path: 'inscription',
    title: 'Inscription – DevBrik',
    loadComponent: () => import('./inscription/inscription.component').then((m) => m.InscriptionComponent),
  },
  {
    path: 'profil',
    title: 'Mon compte – DevBrik',
    canActivate: [connexionGuard],
    loadComponent: () => import('./mon-compte/mon-compte.component').then((m) => m.MonCompteComponent),
  },
];
