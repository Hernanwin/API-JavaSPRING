import { Routes } from '@angular/router';

export const COMPRAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/compras-page/compras-page').then((m) => m.ComprasPage),
  },
];
