import { Routes } from '@angular/router';

// separado del app.routes.ts principal: cuando este feature crezca
// (ej. se sume una pagina de detalle), solo se toca este archivo.
export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/productos-page/productos-page').then((m) => m.ProductosPage),
  },
];
