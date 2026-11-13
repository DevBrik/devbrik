import { inject } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { adminGuard, connexionGuard } from './core/auth/gardes';

/**
 * Table des routes (dossier 12.4). Chaque module est chargé à la demande ;
 * les routes statiques sont déclarées avant les routes à paramètre.
 */
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
    loadChildren: () => import('./features/demandes/developpeurs.routes').then((m) => m.DEVELOPPEURS_ROUTES),
  },
  {
    // Lien « Mon profil » du pied de page : connexion puis profil public de l'utilisateur.
    path: 'compte/mon-profil',
    canActivate: [
      connexionGuard,
      () => {
        const id = inject(AuthService).utilisateur()?.id;
        return inject(Router).createUrlTree(id ? ['/developpeurs', id] : ['/compte/connexion']);
      },
    ],
    children: [],
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
  {
    path: 'acces-refuse',
    title: 'Accès refusé – DevBrik',
    loadComponent: () => import('./shared/pages/acces-refuse/acces-refuse.component').then((m) => m.AccesRefuseComponent),
  },
  {
    path: '**',
    title: 'Page introuvable – DevBrik',
    loadComponent: () =>
      import('./shared/pages/page-introuvable/page-introuvable.component').then((m) => m.PageIntrouvableComponent),
  },
];

