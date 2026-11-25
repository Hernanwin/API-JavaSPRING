import { Routes } from '@angular/router';

export const TIENDAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/tiendas-page/tiendas-page').then((m) => m.TiendasPage),
  },
];
