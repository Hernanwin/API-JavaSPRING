import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InventarioService } from '../../inventario/data/inventario.service';
import { Inventario } from '../../inventario/data/inventario.model';
import { ReporteService } from '../../reportes/data/reporte.service';
import { Movimiento, ResumenStockTienda } from '../../reportes/data/reporte.model';
import { ProductoService } from '../../productos/data/producto.service';
import { Producto } from '../../productos/data/producto.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { AuthStore } from '../../../core/auth/auth.store';
import { estaBajoMinimo } from '../../../shared/utils/stock';
import { calcularVariacionPorcentual, fechaISO, filtrarPorTienda } from '../../../shared/utils/tienda-filtro';

@Injectable({ providedIn: 'root' })
export class ResumenStore {
  private readonly inventarioService = inject(InventarioService);
  private readonly reporteService = inject(ReporteService);
  private readonly productoService = inject(ProductoService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);

  private readonly _inventario = signal<Inventario[]>([]);
  private readonly _productos = signal<Producto[]>([]);
  private readonly _resumenTiendas = signal<ResumenStockTienda[]>([]);
  private readonly _stockBajoTodo = signal<Inventario[]>([]);
  private readonly _movimientos = signal<Movimiento[]>([]);
  private readonly _cargando = signal(false);

  readonly cargando = this._cargando.asReadonly();
  readonly idTiendaSeleccionada = this.seleccion.idTienda;
  readonly tieneReportesOperativos = computed(() =>
    this.authStore.tienePermiso('REPORTE_OPERATIVO_VER'),
  );
  readonly puedeVerCostos = this.authStore.puedeVerCostos;

  constructor() {
    effect(() => {
      const idTienda = this.idTiendaSeleccionada();
      if (this.authStore.autenticado()) {
        void this.cargar(idTienda);
      }
    });
  }

  async cargar(idTienda: number | null = this.idTiendaSeleccionada()): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeInv = this.authStore.tienePermiso('INVENTARIO_VER');
      const puedeCat = this.authStore.tienePermiso('CATALOGO_VER');
      const puedeRep = this.authStore.tienePermiso('REPORTE_OPERATIVO_VER');

      const [inventario, productos, resumenTiendas, stockBajo, movimientos] = await Promise.all([
        puedeInv
          ? idTienda == null
            ? firstValueFrom(this.inventarioService.listarTodo())
            : firstValueFrom(this.inventarioService.listarPorTienda(idTienda))
          : Promise.resolve([] as Inventario[]),
        puedeCat ? firstValueFrom(this.productoService.listar()) : Promise.resolve([] as Producto[]),
        puedeRep
          ? firstValueFrom(this.reporteService.resumenPorTienda())
          : Promise.resolve([] as ResumenStockTienda[]),
        puedeRep
          ? firstValueFrom(this.reporteService.stockBajo())
          : Promise.resolve([] as Inventario[]),
        puedeRep
          ? firstValueFrom(this.reporteService.movimientos({ idTienda, page: 0, size: 50 }))
          : Promise.resolve([] as Movimiento[]),
      ]);

      this._inventario.set(inventario);
      this._productos.set(productos);
      this._resumenTiendas.set(resumenTiendas);
      this._stockBajoTodo.set(
        puedeRep ? stockBajo : inventario.filter((i) => estaBajoMinimo(i.cantidad, i.stockMinimo)),
      );
      this._movimientos.set(movimientos);
    } finally {
      this._cargando.set(false);
    }
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
      const nombre = this._inventario()[0]?.nombreTienda ?? `Ubicación #${id}`;
      return `disponibles en ${nombre}`;
    }
    return `disponibles en ${tiendasUnicas.size} ubicación(es)`;
  });

  readonly valorInventario = computed<number | null>(() => {
    if (!this.authStore.puedeVerCostos()) {
      return null;
    }
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
