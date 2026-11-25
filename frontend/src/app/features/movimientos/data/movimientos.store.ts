import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InventarioService } from '../../inventario/data/inventario.service';
import { Inventario } from '../../inventario/data/inventario.model';
import { ReporteService } from '../../reportes/data/reporte.service';
import { Movimiento } from '../../reportes/data/reporte.model';
import { ProductoService } from '../../productos/data/producto.service';
import { Producto } from '../../productos/data/producto.model';
import { TiendaService } from '../../tiendas/data/tienda.service';
import { Tienda } from '../../tiendas/data/tienda.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { NotificacionStore } from '../../notificaciones/data/notificacion.store';
import { AuthStore } from '../../../core/auth/auth.store';
import { OperacionesService } from './operaciones.service';
import {
  EntradaInput,
  EstadoTransferencia,
  SalidaManualInput,
  Transferencia,
  TransferenciaInput,
} from './operaciones.model';
import { estaBajoMinimo } from '../../../shared/utils/stock';
import { calcularVariacionPorcentual, fechaISO, filtrarPorTienda } from '../../../shared/utils/tienda-filtro';

@Injectable({ providedIn: 'root' })
export class MovimientosStore {
  private readonly inventarioService = inject(InventarioService);
  private readonly reporteService = inject(ReporteService);
  private readonly productoService = inject(ProductoService);
  private readonly tiendaService = inject(TiendaService);
  private readonly operacionesService = inject(OperacionesService);
  private readonly notificacionStore = inject(NotificacionStore);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);

  private readonly _inventario = signal<Inventario[]>([]);
  private readonly _productos = signal<Producto[]>([]);
  private readonly _tiendas = signal<Tienda[]>([]);
  private readonly _catalogoDestinos = signal<Tienda[]>([]);
  private readonly _transferencias = signal<Transferencia[]>([]);
  private readonly _stockBajoTodo = signal<Inventario[]>([]);
  private readonly _movimientos = signal<Movimiento[]>([]);
  private readonly _cargando = signal(false);

  private readonly _estadoTransfFiltro = signal<EstadoTransferencia | null>(null);
  private readonly _idProductoFiltro = signal<number | null>(null);
  private readonly _page = signal(0);
  private readonly _size = signal(10);

  readonly cargando = this._cargando.asReadonly();
  readonly idTiendaSeleccionada = this.seleccion.idTienda;
  readonly productos = this._productos.asReadonly();
  readonly tiendas = this._tiendas.asReadonly();
  readonly catalogoDestinos = this._catalogoDestinos.asReadonly();
  readonly estadoTransfFiltro = this._estadoTransfFiltro.asReadonly();
  readonly idProductoFiltro = this._idProductoFiltro.asReadonly();
  readonly page = this._page.asReadonly();
  readonly size = this._size.asReadonly();

  readonly puedeEntrada = computed(() => this.authStore.tienePermiso('ENTRADA_REGISTRAR'));
  readonly puedeSalidaManual = computed(() =>
    this.authStore.tienePermiso('SALIDA_MANUAL_REGISTRAR'),
  );
  readonly puedeSolicitarTransf = computed(() =>
    this.authStore.tienePermiso('TRANSFERENCIA_SOLICITAR'),
  );
  readonly puedeAprobarTransf = computed(() =>
    this.authStore.tienePermiso('TRANSFERENCIA_APROBAR'),
  );
  readonly puedeHistorial = computed(() => this.authStore.tienePermiso('REPORTE_OPERATIVO_VER'));

  readonly ubicacionesEntrada = computed(() =>
    this._tiendas().filter((t) =>
      this.authStore.tienePermisoEnUbicacion(t.id, 'ENTRADA_REGISTRAR'),
    ),
  );

  readonly ubicacionesSalidaManual = computed(() =>
    this._tiendas().filter((t) =>
      this.authStore.tienePermisoEnUbicacion(t.id, 'SALIDA_MANUAL_REGISTRAR'),
    ),
  );

  readonly transferenciasVisibles = computed(() => {
    const idSel = this.idTiendaSeleccionada();
    const lista = this._transferencias();
    if (idSel == null) return lista;
    return lista.filter((t) => t.idTiendaOrigen === idSel || t.idTiendaDestino === idSel);
  });

  constructor() {
    effect(() => {
      const idTienda = this.idTiendaSeleccionada();
      const idProducto = this._idProductoFiltro();
      const estadoTr = this._estadoTransfFiltro();
      const page = this._page();
      const size = this._size();
      if (this.authStore.autenticado()) {
        void this.cargar(idTienda, idProducto, estadoTr, page, size);
      }
    });
  }

  async cargar(
    idTienda: number | null = this.idTiendaSeleccionada(),
    idProducto: number | null = this._idProductoFiltro(),
    estadoTr: EstadoTransferencia | null = this._estadoTransfFiltro(),
    page: number = this._page(),
    size: number = this._size(),
  ): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeInv = this.authStore.tienePermiso('INVENTARIO_VER');
      const puedeRep = this.authStore.tienePermiso('REPORTE_OPERATIVO_VER');
      const puedeTr = this.authStore.tienePermiso(
        'TRANSFERENCIA_SOLICITAR',
        'TRANSFERENCIA_OPERAR',
        'TRANSFERENCIA_APROBAR',
        'REPORTE_OPERATIVO_VER',
      );
      const puedeCatDest = this.authStore.tienePermiso(
        'TRANSFERENCIA_SOLICITAR',
        'TRANSFERENCIA_OPERAR',
        'TRANSFERENCIA_APROBAR',
        'TIENDA_CONFIGURAR',
      );

      const [inventario, productos, tiendas, destinos, stockBajo, movimientos, transferencias] =
        await Promise.all([
          puedeInv
            ? idTienda == null
              ? firstValueFrom(this.inventarioService.listarTodo())
              : firstValueFrom(this.inventarioService.listarPorTienda(idTienda))
            : Promise.resolve([] as Inventario[]),
          firstValueFrom(this.productoService.listar()),
          firstValueFrom(this.tiendaService.listar()),
          puedeCatDest
            ? firstValueFrom(this.tiendaService.listarCatalogoDestinos())
            : Promise.resolve([] as Tienda[]),
          puedeRep
            ? firstValueFrom(this.reporteService.stockBajo())
            : Promise.resolve([] as Inventario[]),
          puedeRep
            ? firstValueFrom(
                this.reporteService.movimientos({ idTienda, idProducto, page, size }),
              )
            : Promise.resolve([] as Movimiento[]),
          puedeTr
            ? firstValueFrom(this.operacionesService.listarTransferencias(estadoTr))
            : Promise.resolve([] as Transferencia[]),
        ]);

      this._inventario.set(inventario);
      this._productos.set(productos);
      this._tiendas.set(tiendas);
      this._catalogoDestinos.set(destinos.length > 0 ? destinos : tiendas);
      this._stockBajoTodo.set(
        puedeRep ? stockBajo : inventario.filter((i) => estaBajoMinimo(i.cantidad, i.stockMinimo)),
      );
      this._movimientos.set(movimientos);
      this._transferencias.set(transferencias);
    } finally {
      this._cargando.set(false);
    }
  }

  filtrarEstadoTransferencia(estado: EstadoTransferencia | null): void {
    this._estadoTransfFiltro.set(estado);
  }

  filtrarPorProducto(idProducto: number | null): void {
    this._page.set(0);
    this._idProductoFiltro.set(idProducto);
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0) return;
    this._page.set(nuevaPagina);
  }

  cambiarTamanoPagina(nuevoSize: number): void {
    this._page.set(0);
    this._size.set(nuevoSize);
  }

  puedeDespachar(t: Transferencia): boolean {
    return (
      t.estado === 'APROBADA' &&
      this.authStore.tienePermisoEnUbicacion(t.idTiendaOrigen, 'TRANSFERENCIA_OPERAR')
    );
  }

  puedeRecibir(t: Transferencia): boolean {
    return (
      t.estado === 'EN_TRANSITO' &&
      this.authStore.tienePermisoEnUbicacion(
        t.idTiendaDestino,
        'TRANSFERENCIA_OPERAR',
        'ENTRADA_REGISTRAR',
      )
    );
  }

  async registrarEntrada(input: EntradaInput): Promise<void> {
    await firstValueFrom(this.operacionesService.crearEntrada(input));
    await this.cargar();
  }

  async registrarSalidaManual(input: SalidaManualInput): Promise<void> {
    await firstValueFrom(this.operacionesService.crearSalidaManual(input));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
  }

  async solicitarTransferencia(input: TransferenciaInput): Promise<void> {
    await firstValueFrom(this.operacionesService.solicitarTransferencia(input));
    await this.cargar();
  }

  async aprobarTransferencia(id: number): Promise<void> {
    await firstValueFrom(this.operacionesService.aprobarTransferencia(id));
    await this.cargar();
  }

  async rechazarTransferencia(id: number): Promise<void> {
    await firstValueFrom(this.operacionesService.rechazarTransferencia(id, 'Rechazada por supervisor'));
    await this.cargar();
  }

  async enviarTransferencia(id: number): Promise<void> {
    await firstValueFrom(this.operacionesService.enviarTransferencia(id));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
  }

  async recibirTransferencia(
    id: number,
    cantidadRecibida: number,
    observacion?: string,
  ): Promise<void> {
    await firstValueFrom(
      this.operacionesService.recibirTransferencia(id, cantidadRecibida, observacion),
    );
    await this.cargar();
  }

  readonly movimientosHistorial = this._movimientos.asReadonly();

  readonly stockBajoFiltrado = computed(() =>
    filtrarPorTienda(this._stockBajoTodo(), this.idTiendaSeleccionada()),
  );

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
  readonly ventasHoy = computed(() => this.totalSalidas(fechaISO(0)));
  readonly ventasAyer = computed(() => this.totalSalidas(fechaISO(-1)));
  readonly variacionVentas = computed(() =>
    calcularVariacionPorcentual(this.ventasHoy(), this.ventasAyer()),
  );

  private totalSalidas(fecha: string): number {
    return this._movimientos()
      .filter((m) => m.tipo === 'SALIDA')
      .filter((m) => m.fecha?.startsWith(fecha))
      .reduce((acc, m) => acc + m.cantidad, 0);
  }
}
