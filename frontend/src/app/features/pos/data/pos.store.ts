import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { PosService } from './pos.service';
import { Apartado, ApartadoCrearInput, EstadoApartado } from './pos.model';
import { OperacionesService } from '../../movimientos/data/operaciones.service';
import {
  Venta,
  VentaActualizarInput,
  VentaCrearInput,
} from '../../movimientos/data/operaciones.model';
import { InventarioService } from '../../inventario/data/inventario.service';
import { Inventario } from '../../inventario/data/inventario.model';
import { ProductoService } from '../../productos/data/producto.service';
import { Producto } from '../../productos/data/producto.model';
import { TiendaService } from '../../tiendas/data/tienda.service';
import { Tienda } from '../../tiendas/data/tienda.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { AuthStore } from '../../../core/auth/auth.store';
import { NotificacionStore } from '../../notificaciones/data/notificacion.store';
import { fechaISO, filtrarPorTienda } from '../../../shared/utils/tienda-filtro';

@Injectable({ providedIn: 'root' })
export class PosStore {
  private readonly posService = inject(PosService);
  private readonly operacionesService = inject(OperacionesService);
  private readonly inventarioService = inject(InventarioService);
  private readonly productoService = inject(ProductoService);
  private readonly tiendaService = inject(TiendaService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);
  private readonly notificacionStore = inject(NotificacionStore);

  private readonly _tiendas = signal<Tienda[]>([]);
  private readonly _productos = signal<Producto[]>([]);
  private readonly _inventario = signal<Inventario[]>([]);
  private readonly _ventas = signal<Venta[]>([]);
  private readonly _apartados = signal<Apartado[]>([]);
  private readonly _estadoApartadoFiltro = signal<EstadoApartado | null>(null);
  private readonly _cargando = signal(false);

  readonly cargando = this._cargando.asReadonly();
  readonly productos = this._productos.asReadonly();
  readonly estadoApartadoFiltro = this._estadoApartadoFiltro.asReadonly();

  readonly sucursalesPos = computed(() =>
    this._tiendas().filter(
      (t) =>
        t.tipo !== 'DEPOSITO' &&
        this.authStore.tienePermisoEnUbicacion(
          t.id,
          'POS_COBRAR',
          'APARTADO_GESTIONAR',
          'VENTA_ADMINISTRAR',
        ),
    ),
  );

  readonly idSucursalActiva = computed<number | null>(() => {
    const sel = this.seleccion.idTienda();
    const lista = this.sucursalesPos();
    if (sel != null && lista.some((t) => t.id === sel)) return sel;
    return lista[0]?.id ?? null;
  });

  readonly cajaActiva = computed(() =>
    this.authStore.cajaAsignadaEn(this.idSucursalActiva()),
  );

  readonly puedeCobrar = computed(() =>
    this.authStore.tienePermisoEnUbicacion(this.idSucursalActiva(), 'POS_COBRAR'),
  );

  readonly puedeApartar = computed(() =>
    this.authStore.tienePermisoEnUbicacion(this.idSucursalActiva(), 'APARTADO_GESTIONAR'),
  );

  constructor() {
    effect(() => {
      const idGlobal = this.seleccion.idTienda();
      const estAp = this._estadoApartadoFiltro();
      if (this.authStore.autenticado()) {
        void this.cargar(idGlobal, estAp);
      }
    });
  }

  async cargar(
    idTiendaFiltro: number | null = this.seleccion.idTienda(),
    estadoApartado: EstadoApartado | null = this._estadoApartadoFiltro(),
  ): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeInv = this.authStore.tienePermiso('INVENTARIO_VER');
      const puedeVentas = this.authStore.tienePermiso(
        'POS_COBRAR',
        'VENTA_ADMINISTRAR',
        'REPORTE_FINANCIERO_CONSOLIDADO_VER',
      );
      const puedeAp = this.authStore.tienePermiso('APARTADO_GESTIONAR', 'VENTA_ADMINISTRAR');

      const [tiendas, productos, inventario, ventas, apartados] = await Promise.all([
        firstValueFrom(this.tiendaService.listar()),
        firstValueFrom(this.productoService.listar()),
        puedeInv
          ? idTiendaFiltro == null
            ? firstValueFrom(this.inventarioService.listarTodo())
            : firstValueFrom(this.inventarioService.listarPorTienda(idTiendaFiltro))
          : Promise.resolve([] as Inventario[]),
        puedeVentas
          ? firstValueFrom(this.operacionesService.listarVentas())
          : Promise.resolve([] as Venta[]),
        puedeAp
          ? firstValueFrom(
              this.posService.listarApartados({
                idTienda: idTiendaFiltro,
                estado: estadoApartado,
              }),
            )
          : Promise.resolve([] as Apartado[]),
      ]);

      this._tiendas.set(tiendas);
      this._productos.set(productos);
      this._inventario.set(inventario);
      this._ventas.set(ventas);
      this._apartados.set(apartados);
    } finally {
      this._cargando.set(false);
    }
  }

  filtrarEstadoApartado(estado: EstadoApartado | null): void {
    this._estadoApartadoFiltro.set(estado);
  }

  puedeAdministrarVenta(venta: Venta): boolean {
    return this.authStore.tienePermisoEnUbicacion(venta.idTienda, 'VENTA_ADMINISTRAR');
  }

  stockDisponibleEnSucursal(
    idProducto: number | null | undefined,
    idTienda: number | null | undefined,
  ): number {
    if (idProducto == null || idTienda == null) return 0;
    const fila = this._inventario().find(
      (i) => i.idProducto === idProducto && i.idTienda === idTienda,
    );
    if (!fila) return 0;
    return fila.cantidadDisponible ?? fila.cantidad;
  }

  precioProducto(idProducto: number | null | undefined): number {
    if (idProducto == null) return 0;
    return this._productos().find((p) => p.id === idProducto)?.precio ?? 0;
  }

  nombreTienda(idTienda: number): string {
    return this._tiendas().find((t) => t.id === idTienda)?.nombre ?? `Sucursal #${idTienda}`;
  }

  readonly inventarioSucursal = computed(() =>
    filtrarPorTienda(this._inventario(), this.seleccion.idTienda()),
  );

  readonly ventasFiltradas = computed(() =>
    filtrarPorTienda(this._ventas(), this.seleccion.idTienda()),
  );

  readonly apartadosFiltrados = computed(() =>
    filtrarPorTienda(this._apartados(), this.seleccion.idTienda()),
  );

  readonly facturacionHoy = computed(() => {
    const hoy = fechaISO(0);
    return this.ventasFiltradas()
      .filter((v) => v.fechaHora?.startsWith(hoy))
      .reduce((acc, v) => acc + v.total, 0);
  });

  readonly apartadosActivosCount = computed(
    () => this.apartadosFiltrados().filter((a) => a.estado === 'ACTIVO').length,
  );

  readonly unidadesDisponiblesSucursal = computed(() =>
    this.inventarioSucursal().reduce((acc, i) => acc + (i.cantidadDisponible ?? i.cantidad), 0),
  );

  async registrarVenta(input: VentaCrearInput): Promise<Venta> {
    const creada = await firstValueFrom(this.operacionesService.crearVenta(input));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
    return creada;
  }

  async actualizarVenta(id: number, input: VentaActualizarInput): Promise<void> {
    await firstValueFrom(this.operacionesService.actualizarVenta(id, input));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
  }

  async eliminarVenta(id: number): Promise<void> {
    await firstValueFrom(this.operacionesService.eliminarVenta(id));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
  }

  async crearApartado(input: ApartadoCrearInput): Promise<Apartado> {
    const creado = await firstValueFrom(this.posService.crearApartado(input));
    await this.cargar();
    return creado;
  }

  async cobrarApartado(id: number): Promise<Apartado> {
    const cobrado = await firstValueFrom(this.posService.cobrarApartado(id));
    await Promise.all([this.cargar(), this.notificacionStore.cargar()]);
    return cobrado;
  }

  async cancelarApartado(id: number): Promise<void> {
    await firstValueFrom(this.posService.cancelarApartado(id));
    await this.cargar();
  }
}
