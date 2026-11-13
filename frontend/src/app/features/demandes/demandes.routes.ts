import { Routes } from '@angular/router';
import { connexionGuard } from '../../core/auth/gardes';

/** Module demandes (membre 4) : M06, M07, M08. */
export const DEMANDES_ROUTES: Routes = [
  {
    path: '',
    title: 'Les demandes – DevBrik',
    loadComponent: () => import('./liste/liste-demandes.component').then((m) => m.ListeDemandesComponent),
  },
  {
    path: 'nouvelle',
    title: 'Nouvelle demande – DevBrik',
    canActivate: [connexionGuard],
    loadComponent: () => import('./nouvelle/nouvelle-demande.component').then((m) => m.NouvelleDemandeComponent),
  },
  {
    path: ':id',
    title: 'Détail de la demande – DevBrik',
    loadComponent: () => import('./detail/detail-demande.component').then((m) => m.DetailDemandeComponent),
  },
];
