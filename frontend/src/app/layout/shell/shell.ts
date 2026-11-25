import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { TiendaStore } from '../../features/tiendas/data/tienda.store';
import { TiendaSeleccionadaStore } from '../../core/state/tienda-seleccionada.store';
import { NotificacionStore } from '../../features/notificaciones/data/notificacion.store';
import { AuthStore } from '../../core/auth/auth.store';
import { ETIQUETA_ROL, NivelRiesgo, Permiso } from '../../core/auth/auth.model';

interface NavSeccion {
  titulo: string;
  items: { path: string; label: string; icon: string; visible: boolean }[];
}

@Component({
  selector: 'app-shell',
  imports: [
    DatePipe,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    FormsModule,
    SelectModule,
    DialogModule,
    ButtonModule,
    TagModule,
  ],
  templateUrl: './shell.html',
})
export class Shell {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  protected readonly authStore = inject(AuthStore);
  protected readonly tiendaStore = inject(TiendaStore);
  protected readonly seleccion = inject(TiendaSeleccionadaStore);
  protected readonly notificacionStore = inject(NotificacionStore);

  protected readonly notificacionesVisible = signal(false);
  protected readonly alcanceVisible = signal(false);
  protected readonly etiquetaRol = ETIQUETA_ROL;

  protected readonly seccionesNav = computed<NavSeccion[]>(() => {
    const tiene = (...p: Permiso[]) => this.authStore.tienePermiso(...p);
    const secciones: NavSeccion[] = [
      {
        titulo: 'Operación',
        items: [
          { path: '/resumen', label: 'Resumen', icon: 'pi pi-th-large', visible: true },
          {
            path: '/pos',
            label: 'POS y Apartados',
            icon: 'pi pi-shopping-cart',
            visible: tiene('POS_COBRAR', 'APARTADO_GESTIONAR', 'VENTA_ADMINISTRAR'),
          },
          {
            path: '/inventario',
            label: 'Inventario y Conteos',
            icon: 'pi pi-table',
            visible: tiene('INVENTARIO_VER', 'AJUSTE_CONTEO_EJECUTAR'),
          },
          {
            path: '/movimientos',
            label: 'Depósito y Envíos',
            icon: 'pi pi-arrows-h',
            visible: tiene(
              'ENTRADA_REGISTRAR',
              'SALIDA_MANUAL_REGISTRAR',
              'TRANSFERENCIA_SOLICITAR',
              'TRANSFERENCIA_APROBAR',
              'TRANSFERENCIA_OPERAR',
              'REPORTE_OPERATIVO_VER',
            ),
          },
          {
            path: '/compras',
            label: 'Compras y Recepción',
            icon: 'pi pi-truck',
            visible: tiene(
              'PROVEEDOR_GESTIONAR',
              'ORDEN_COMPRA_VER',
              'ORDEN_COMPRA_CREAR',
              'ORDEN_COMPRA_APROBAR',
              'ENTRADA_REGISTRAR',
            ),
          },
        ],
      },
      {
        titulo: 'Catálogo y Red',
        items: [
          {
            path: '/productos',
            label: 'Productos',
            icon: 'pi pi-box',
            visible: tiene('CATALOGO_VER'),
          },
          {
            path: '/tiendas',
            label: 'Ubicaciones',
            icon: 'pi pi-building',
            visible: true,
          },
        ],
      },
      {
        titulo: 'Analítica y Gobierno',
        items: [
          {
            path: '/reportes',
            label: 'Reportes y Márgenes',
            icon: 'pi pi-chart-bar',
            visible: tiene('REPORTE_ROTACION_MARGENES_VER', 'REPORTE_FINANCIERO_CONSOLIDADO_VER'),
          },
          {
            path: '/usuarios',
            label: 'Usuarios y Roles',
            icon: 'pi pi-users',
            visible: tiene('USUARIO_GESTIONAR'),
          },
        ],
      },
    ];
    return secciones
      .map((s) => ({ ...s, items: s.items.filter((i) => i.visible) }))
      .filter((s) => s.items.length > 0);
  });

  protected readonly breadcrumb = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(() => this.leerBreadcrumb()),
      startWith(this.leerBreadcrumb()),
    ),
    { initialValue: 'Resumen Operativo' },
  );

  protected readonly opcionesTienda = computed(() => {
    const tiendas = this.tiendaStore.tiendas();
    const items = tiendas.map((t) => ({
      id: t.id as number | null,
      nombre: `${t.tipo === 'DEPOSITO' ? '[DEP]' : '[POS]'} ${t.nombre}`,
    }));
    if (this.authStore.esAlcanceGlobal()) {
      return [{ id: null, nombre: 'Todas las ubicaciones (Global)' }, ...items];
    }
    if (items.length > 1) {
      return [{ id: null, nombre: `Mis ubicaciones (${items.length})` }, ...items];
    }
    return items;
  });

  constructor() {
    void this.tiendaStore.cargar();
  }

  protected abrirNotificaciones(): void {
    void this.notificacionStore.cargar();
    this.notificacionesVisible.set(true);
  }

  protected severidadRiesgo(nivel?: NivelRiesgo): 'danger' | 'warn' | 'info' | 'success' {
    switch (nivel) {
      case 'CRITICO':
        return 'danger';
      case 'ALTO':
        return 'warn';
      case 'MEDIO':
        return 'info';
      default:
        return 'success';
    }
  }

  private leerBreadcrumb(): string {
    let ruta: ActivatedRoute | null = this.router.routerState.root;
    let breadcrumb = '';
    while (ruta) {
      const valor = ruta.snapshot?.data?.['breadcrumb'];
      if (valor) breadcrumb = valor as string;
      ruta = ruta.firstChild;
    }
    return breadcrumb || 'Panel';
  }
}
