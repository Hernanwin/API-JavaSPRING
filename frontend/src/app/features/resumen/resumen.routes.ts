import { Routes } from '@angular/router';

export const RESUMEN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/resumen-page/resumen-page').then((m) => m.ResumenPage),
  },
];
