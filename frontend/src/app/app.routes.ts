import { Routes } from '@angular/router';
import { authGuard, guestGuard, permisoGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'resumen', pathMatch: 'full' },
      {
        path: 'resumen',
        data: { breadcrumb: 'Resumen Operativo' },
        loadChildren: () => import('./features/resumen/resumen.routes').then((m) => m.RESUMEN_ROUTES),
      },
      {
        path: 'pos',
        canActivate: [permisoGuard('POS_COBRAR', 'APARTADO_GESTIONAR', 'VENTA_ADMINISTRAR')],
        data: { breadcrumb: 'POS Caja y Apartados' },
        loadChildren: () => import('./features/pos/pos.routes').then((m) => m.POS_ROUTES),
      },
      {
        path: 'inventario',
        canActivate: [permisoGuard('INVENTARIO_VER', 'AJUSTE_CONTEO_EJECUTAR')],
        data: { breadcrumb: 'Inventario y Conteos' },
        loadChildren: () =>
          import('./features/inventario/inventario.routes').then((m) => m.INVENTARIO_ROUTES),
      },
      {
        path: 'movimientos',
        canActivate: [
          permisoGuard(
            'ENTRADA_REGISTRAR',
            'SALIDA_MANUAL_REGISTRAR',
            'TRANSFERENCIA_SOLICITAR',
            'TRANSFERENCIA_APROBAR',
            'TRANSFERENCIA_OPERAR',
            'REPORTE_OPERATIVO_VER',
          ),
        ],
        data: { breadcrumb: 'Depósito y Transferencias' },
        loadChildren: () =>
          import('./features/movimientos/movimientos.routes').then((m) => m.MOVIMIENTOS_ROUTES),
      },
      {
        path: 'compras',
        canActivate: [
          permisoGuard(
            'PROVEEDOR_GESTIONAR',
            'ORDEN_COMPRA_VER',
            'ORDEN_COMPRA_CREAR',
            'ORDEN_COMPRA_APROBAR',
            'ENTRADA_REGISTRAR',
          ),
        ],
        data: { breadcrumb: 'Proveedores y Órdenes de Compra' },
        loadChildren: () => import('./features/compras/compras.routes').then((m) => m.COMPRAS_ROUTES),
      },
      {
        path: 'productos',
        canActivate: [permisoGuard('CATALOGO_VER')],
        data: { breadcrumb: 'Catálogo de Productos' },
        loadChildren: () =>
          import('./features/productos/productos.routes').then((m) => m.PRODUCTOS_ROUTES),
      },
      {
        path: 'tiendas',
        data: { breadcrumb: 'Sucursales y Depósitos' },
        loadChildren: () => import('./features/tiendas/tiendas.routes').then((m) => m.TIENDAS_ROUTES),
      },
      {
        path: 'reportes',
        canActivate: [
          permisoGuard('REPORTE_ROTACION_MARGENES_VER', 'REPORTE_FINANCIERO_CONSOLIDADO_VER'),
        ],
        data: { breadcrumb: 'Reportes, Márgenes y Cumplimiento' },
        loadChildren: () =>
          import('./features/reportes/reportes.routes').then((m) => m.REPORTES_ROUTES),
      },
      {
        path: 'usuarios',
        canActivate: [permisoGuard('USUARIO_GESTIONAR')],
        data: { breadcrumb: 'Usuarios, Roles y Alcance' },
        loadChildren: () =>
          import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
