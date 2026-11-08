import { Routes } from '@angular/router';

/** Routes M11 et M12. adminGuard est posé sur le parent `admin` dans app.routes.ts. */
export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'signalements' },
  {
    path: 'signalements',
    title: 'Administration : signalements – DevBrik',
    loadComponent: () =>
      import('./admin/m11-signalements/signalements.component').then((m) => m.SignalementsComponent),
  },
  {
    path: 'statistiques',
    title: 'Administration : statistiques – DevBrik',
    loadComponent: () =>
      import('./admin/m12-statistiques/statistiques.component').then((m) => m.StatistiquesComponent),
  },
];
