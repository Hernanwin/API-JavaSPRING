import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InventarioService } from './inventario.service';
import {
  AjusteConteo,
  AjusteConteoInput,
  Inventario,
  InventarioConsolidado,
} from './inventario.model';
import { ReporteService } from '../../reportes/data/reporte.service';
import { Movimiento } from '../../reportes/data/reporte.model';
import { ProductoService } from '../../productos/data/producto.service';
import { Producto } from '../../productos/data/producto.model';
import { TiendaService } from '../../tiendas/data/tienda.service';
import { Tienda } from '../../tiendas/data/tienda.model';
import { ComprasService } from '../../compras/data/compras.service';
import { Proveedor } from '../../compras/data/compras.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { AuthStore } from '../../../core/auth/auth.store';
import { NotificacionStore } from '../../notificaciones/data/notificacion.store';
import { estaBajoMinimo } from '../../../shared/utils/stock';
import { calcularVariacionPorcentual, fechaISO, filtrarPorTienda } from '../../../shared/utils/tienda-filtro';

@Injectable({ providedIn: 'root' })
export class InventarioStore {
  private readonly inventarioService = inject(InventarioService);
  private readonly reporteService = inject(ReporteService);
  private readonly productoService = inject(ProductoService);
  private readonly tiendaService = inject(TiendaService);
  private readonly comprasService = inject(ComprasService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);
  private readonly notificacionStore = inject(NotificacionStore);

  private readonly _inventario = signal<Inventario[]>([]);
  private readonly _consolidado = signal<InventarioConsolidado[]>([]);
  private readonly _ajustes = signal<AjusteConteo[]>([]);
  private readonly _productos = signal<Producto[]>([]);
  private readonly _tiendas = signal<Tienda[]>([]);
  private readonly _proveedores = signal<Proveedor[]>([]);
  private readonly _stockBajoTodo = signal<Inventario[]>([]);
  private readonly _movimientos = signal<Movimiento[]>([]);
  private readonly _idProveedorFiltro = signal<number | null>(null);
  private readonly _cargando = signal(false);

  readonly cargando = this._cargando.asReadonly();
  readonly idTiendaSeleccionada = this.seleccion.idTienda;
  readonly productos = this._productos.asReadonly();
  readonly tiendas = this._tiendas.asReadonly();
  readonly proveedores = this._proveedores.asReadonly();
  readonly consolidado = this._consolidado.asReadonly();
  readonly ajustes = this._ajustes.asReadonly();
  readonly idProveedorFiltro = this._idProveedorFiltro.asReadonly();

  readonly puedeVerConsolidado = computed(() =>
    this.authStore.tieneAlcanceGlobal('INVENTARIO_VER'),
  );
  readonly puedeVerConteos = computed(() =>
    this.authStore.tienePermiso('AJUSTE_CONTEO_EJECUTAR', 'REPORTE_OPERATIVO_VER'),
  );
  readonly puedeEjecutarConteo = computed(() =>
    this.authStore.tienePermiso('AJUSTE_CONTEO_EJECUTAR'),
  );
  readonly puedeVerCostos = this.authStore.puedeVerCostos;

  readonly ubicacionesParaConteo = computed(() =>
    this._tiendas().filter((t) =>
      this.authStore.tienePermisoEnUbicacion(t.id, 'AJUSTE_CONTEO_EJECUTAR'),
    ),
  );

  constructor() {
    effect(() => {
      const idTienda = this.idTiendaSeleccionada();
      const idProveedor = this._idProveedorFiltro();
      if (this.authStore.autenticado()) {
        void this.cargar(idTienda, idProveedor);
      }
    });
  }

  async cargar(
    idTienda: number | null = this.idTiendaSeleccionada(),
    idProveedor: number | null = this._idProveedorFiltro(),
  ): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeRep = this.authStore.tienePermiso('REPORTE_OPERATIVO_VER');
      const puedeCons = this.puedeVerConsolidado();
      const puedeCont = this.puedeVerConteos();
      const puedeProv = this.authStore.tienePermiso('PROVEEDOR_GESTIONAR', 'ORDEN_COMPRA_VER');

      const [inventario, productos, tiendas, stockBajo, movimientos, consolidado, ajustes, provs] =
        await Promise.all([
          idTienda == null
            ? firstValueFrom(this.inventarioService.listarTodo())
            : firstValueFrom(this.inventarioService.listarPorTienda(idTienda)),
          firstValueFrom(this.productoService.listar()),
          firstValueFrom(this.tiendaService.listar()),
          puedeRep
            ? firstValueFrom(this.reporteService.stockBajo())
            : Promise.resolve([] as Inventario[]),
          puedeRep
            ? firstValueFrom(this.reporteService.movimientos({ idTienda, page: 0, size: 50 }))
            : Promise.resolve([] as Movimiento[]),
          puedeCons
            ? firstValueFrom(this.inventarioService.listarConsolidado(idProveedor))
            : Promise.resolve([] as InventarioConsolidado[]),
          puedeCont
            ? firstValueFrom(this.inventarioService.listarAjustesConteo(idTienda))
            : Promise.resolve([] as AjusteConteo[]),
          puedeProv
            ? firstValueFrom(this.comprasService.listarProveedores())
            : Promise.resolve([] as Proveedor[]),
        ]);

      this._inventario.set(inventario);
      this._productos.set(productos);
      this._tiendas.set(tiendas);
      this._stockBajoTodo.set(
        puedeRep ? stockBajo : inventario.filter((i) => estaBajoMinimo(i.cantidad, i.stockMinimo)),
      );
      this._movimientos.set(movimientos);
      this._consolidado.set(consolidado);
      this._ajustes.set(ajustes);
      this._proveedores.set(provs);
    } finally {
      this._cargando.set(false);
    }
  }

  filtrarProveedorConsolidado(idProveedor: number | null): void {
    this._idProveedorFiltro.set(idProveedor);
  }

  async actualizarStockMinimo(
    idProducto: number,
    idTienda: number,
    stockMinimo: number,
  ): Promise<void> {
    await firstValueFrom(
      this.inventarioService.actualizarStockMinimo(idProducto, idTienda, stockMinimo),
    );
    await this.cargar();
  }

  async registrarAjusteConteo(input: AjusteConteoInput): Promise<AjusteConteo> {
    const creado = await firstValueFrom(this.inventarioService.registrarAjusteConteo(input));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
    return creado;
  }

  readonly inventarioFiltrado = this._inventario.asReadonly();

  readonly stockBajoFiltrado = computed(() =>
    filtrarPorTienda(this._stockBajoTodo(), this.idTiendaSeleccionada()),
  );

  readonly movimientosFiltrados = computed(() => this._movimientos().slice(0, 8));

  readonly unidadesEnStock = computed(() =>
    this._inventario().reduce((acc, i) => acc + (i.cantidadDisponible ?? i.cantidad), 0),
  );

  readonly etiquetaAlcance = computed(() => {
    const tiendasUnicas = new Set(this._inventario().map((i) => i.idTienda));
    const id = this.idTiendaSeleccionada();
    if (id != null) {
      const nombre = this._tiendas().find((t) => t.id === id)?.nombre ?? `Ubicación #${id}`;
      return `disponibles en ${nombre}`;
    }
    return `disponibles en ${tiendasUnicas.size} ubicación(es)`;
  });

  readonly valorInventario = computed<number | null>(() => {
    if (!this.authStore.puedeVerCostos()) return null;
    return this._inventario().reduce(
      (acc, item) => acc + (item.valorCostoTotal ?? item.cantidad * (item.costoUnitario ?? 0)),
      0,
    );
  });

  readonly alertasStockBajo = computed(() => this.stockBajoFiltrado().length);
  readonly ventasHoy = computed(() => this.totalVentas(fechaISO(0)));
  readonly ventasAyer = computed(() => this.totalVentas(fechaISO(-1)));
  readonly variacionVentas = computed(() =>
    calcularVariacionPorcentual(this.ventasHoy(), this.ventasAyer()),
  );

  private totalVentas(fecha: string): number {
    return this._movimientos()
      .filter((m) => m.tipo === 'SALIDA')
      .filter((m) => m.fecha?.startsWith(fecha))
      .reduce((acc, m) => acc + m.cantidad, 0);
  }
}
