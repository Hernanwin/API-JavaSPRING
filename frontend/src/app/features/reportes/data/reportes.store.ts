import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ReporteService } from './reporte.service';
import {
  ReporteCumplimientoRecepcion,
  ReporteFinancieroConsolidado,
  ReporteMargenProducto,
  ReporteRotacion,
} from './reporte.model';
import { ComprasService } from '../../compras/data/compras.service';
import { Proveedor } from '../../compras/data/compras.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { AuthStore } from '../../../core/auth/auth.store';

@Injectable({ providedIn: 'root' })
export class ReportesStore {
  private readonly reporteService = inject(ReporteService);
  private readonly comprasService = inject(ComprasService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);

  private readonly _margenes = signal<ReporteMargenProducto[]>([]);
  private readonly _rotacion = signal<ReporteRotacion[]>([]);
  private readonly _cumplimiento = signal<ReporteCumplimientoRecepcion[]>([]);
  private readonly _financiero = signal<ReporteFinancieroConsolidado | null>(null);
  private readonly _proveedores = signal<Proveedor[]>([]);
  private readonly _idProveedorFiltro = signal<number | null>(null);
  private readonly _diasRotacion = signal<number>(30);
  private readonly _cargando = signal(false);

  readonly margenes = this._margenes.asReadonly();
  readonly rotacion = this._rotacion.asReadonly();
  readonly cumplimiento = this._cumplimiento.asReadonly();
  readonly financiero = this._financiero.asReadonly();
  readonly proveedores = this._proveedores.asReadonly();
  readonly idProveedorFiltro = this._idProveedorFiltro.asReadonly();
  readonly diasRotacion = this._diasRotacion.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  readonly puedeMargenesYRotacion = computed(() =>
    this.authStore.tienePermiso('REPORTE_ROTACION_MARGENES_VER'),
  );

  readonly puedeFinancieroConsolidado = computed(() =>
    this.authStore.tieneAlcanceGlobal('REPORTE_FINANCIERO_CONSOLIDADO_VER'),
  );

  constructor() {
    effect(() => {
      const idTienda = this.seleccion.idTienda();
      const idProveedor = this._idProveedorFiltro();
      const dias = this._diasRotacion();
      if (this.authStore.autenticado()) {
        void this.cargar(idTienda, idProveedor, dias);
      }
    });
  }

  async cargar(
    idTienda: number | null = this.seleccion.idTienda(),
    idProveedor: number | null = this._idProveedorFiltro(),
    dias: number = this._diasRotacion(),
  ): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeMr = this.puedeMargenesYRotacion();
      const puedeFin = this.puedeFinancieroConsolidado();
      const puedeProv = this.authStore.tienePermiso('PROVEEDOR_GESTIONAR', 'ORDEN_COMPRA_VER');

      const [margenes, rotacion, cumplimiento, financiero, provs] = await Promise.all([
        puedeMr && this.authStore.puedeVerCostos()
          ? firstValueFrom(this.reporteService.margenes({ idProveedor, idTienda }))
          : Promise.resolve([] as ReporteMargenProducto[]),
        puedeMr
          ? firstValueFrom(this.reporteService.rotacion({ idTienda, idProveedor, dias }))
          : Promise.resolve([] as ReporteRotacion[]),
        puedeMr
          ? firstValueFrom(this.reporteService.cumplimientoRecepcion({ idProveedor, idTienda }))
          : Promise.resolve([] as ReporteCumplimientoRecepcion[]),
        puedeFin
          ? firstValueFrom(this.reporteService.financieroConsolidado())
          : Promise.resolve(null),
        puedeProv
          ? firstValueFrom(this.comprasService.listarProveedores())
          : Promise.resolve([] as Proveedor[]),
      ]);

      this._margenes.set(margenes);
      this._rotacion.set(rotacion);
      this._cumplimiento.set(cumplimiento);
      this._financiero.set(financiero);
      this._proveedores.set(provs);
    } finally {
      this._cargando.set(false);
    }
  }

  filtrarProveedor(idProveedor: number | null): void {
    this._idProveedorFiltro.set(idProveedor);
  }

  cambiarDiasRotacion(dias: number): void {
    this._diasRotacion.set(dias);
  }
}
