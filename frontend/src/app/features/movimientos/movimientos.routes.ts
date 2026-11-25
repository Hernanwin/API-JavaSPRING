import { Routes } from '@angular/router';

export const MOVIMIENTOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/movimientos-page/movimientos-page').then((m) => m.MovimientosPage),
  },
];
