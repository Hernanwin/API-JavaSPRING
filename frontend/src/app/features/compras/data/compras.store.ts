import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ComprasService } from './compras.service';
import {
  EstadoOrdenCompra,
  OrdenCompra,
  OrdenCompraInput,
  Proveedor,
  ProveedorInput,
  RecepcionOrdenCompraInput,
} from './compras.model';
import { ProductoService } from '../../productos/data/producto.service';
import { Producto } from '../../productos/data/producto.model';
import { TiendaService } from '../../tiendas/data/tienda.service';
import { Tienda } from '../../tiendas/data/tienda.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';
import { AuthStore } from '../../../core/auth/auth.store';

@Injectable({ providedIn: 'root' })
export class ComprasStore {
  private readonly comprasService = inject(ComprasService);
  private readonly productoService = inject(ProductoService);
  private readonly tiendaService = inject(TiendaService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);
  private readonly authStore = inject(AuthStore);

  private readonly _proveedores = signal<Proveedor[]>([]);
  private readonly _ordenes = signal<OrdenCompra[]>([]);
  private readonly _productos = signal<Producto[]>([]);
  private readonly _destinos = signal<Tienda[]>([]);
  private readonly _estadoFiltro = signal<EstadoOrdenCompra | null>(null);
  private readonly _idProveedorFiltro = signal<number | null>(null);
  private readonly _cargando = signal(false);

  readonly proveedores = this._proveedores.asReadonly();
  readonly ordenes = this._ordenes.asReadonly();
  readonly productos = this._productos.asReadonly();
  readonly destinos = this._destinos.asReadonly();
  readonly estadoFiltro = this._estadoFiltro.asReadonly();
  readonly idProveedorFiltro = this._idProveedorFiltro.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  readonly puedeVerCostos = this.authStore.puedeVerCostos;
  readonly puedeVerProveedores = computed(() =>
    this.authStore.tienePermiso('PROVEEDOR_GESTIONAR', 'ORDEN_COMPRA_VER'),
  );
  readonly puedeGestionarProveedores = computed(() =>
    this.authStore.tienePermiso('PROVEEDOR_GESTIONAR'),
  );
  readonly puedeCrearOrden = computed(() => this.authStore.tienePermiso('ORDEN_COMPRA_CREAR'));
  readonly puedeAprobarOrden = computed(() => this.authStore.tienePermiso('ORDEN_COMPRA_APROBAR'));

  constructor() {
    effect(() => {
      const idTienda = this.seleccion.idTienda();
      const estado = this._estadoFiltro();
      const idProv = this._idProveedorFiltro();
      if (this.authStore.autenticado()) {
        void this.cargar(idTienda, estado, idProv);
      }
    });
  }

  async cargar(
    idTiendaDestino: number | null = this.seleccion.idTienda(),
    estado: EstadoOrdenCompra | null = this._estadoFiltro(),
    idProveedor: number | null = this._idProveedorFiltro(),
  ): Promise<void> {
    this._cargando.set(true);
    try {
      const puedeProv = this.puedeVerProveedores();
      const puedeCatDest = this.authStore.tienePermiso(
        'ORDEN_COMPRA_CREAR',
        'TIENDA_CONFIGURAR',
        'TRANSFERENCIA_SOLICITAR',
      );

      const [ordenes, proveedores, productos, destinos] = await Promise.all([
        firstValueFrom(
          this.comprasService.listarOrdenes({ estado, idProveedor, idTiendaDestino }),
        ),
        puedeProv
          ? firstValueFrom(this.comprasService.listarProveedores())
          : Promise.resolve([] as Proveedor[]),
        firstValueFrom(this.productoService.listar()),
        puedeCatDest
          ? firstValueFrom(this.tiendaService.listarCatalogoDestinos())
          : firstValueFrom(this.tiendaService.listar()),
      ]);

      this._ordenes.set(ordenes);
      this._proveedores.set(proveedores);
      this._productos.set(productos);
      this._destinos.set(destinos);
    } finally {
      this._cargando.set(false);
    }
  }

  filtrarEstado(estado: EstadoOrdenCompra | null): void {
    this._estadoFiltro.set(estado);
  }

  filtrarProveedor(idProveedor: number | null): void {
    this._idProveedorFiltro.set(idProveedor);
  }

  puedeRecibirOrden(oc: OrdenCompra): boolean {
    return (
      (oc.estado === 'APROBADA' || oc.estado === 'RECIBIDA_PARCIAL') &&
      this.authStore.tienePermisoEnUbicacion(
        oc.idTiendaDestino,
        'ENTRADA_REGISTRAR',
        'TRANSFERENCIA_OPERAR',
      )
    );
  }

  async crearProveedor(input: ProveedorInput): Promise<void> {
    await firstValueFrom(this.comprasService.crearProveedor(input));
    await this.cargar();
  }

  async actualizarProveedor(id: number, input: ProveedorInput): Promise<void> {
    await firstValueFrom(this.comprasService.actualizarProveedor(id, input));
    await this.cargar();
  }

  async eliminarProveedor(id: number): Promise<void> {
    await firstValueFrom(this.comprasService.eliminarProveedor(id));
    await this.cargar();
  }

  async crearOrden(input: OrdenCompraInput): Promise<OrdenCompra> {
    const creada = await firstValueFrom(this.comprasService.crearOrden(input));
    await this.cargar();
    return creada;
  }

  async aprobarOrden(id: number): Promise<void> {
    await firstValueFrom(this.comprasService.aprobarOrden(id));
    await this.cargar();
  }

  async cancelarOrden(id: number): Promise<void> {
    await firstValueFrom(this.comprasService.cancelarOrden(id));
    await this.cargar();
  }

  async registrarRecepcion(id: number, recepcion: RecepcionOrdenCompraInput): Promise<OrdenCompra> {
    const actualizada = await firstValueFrom(
      this.comprasService.registrarRecepcion(id, recepcion),
    );
    await this.cargar();
    return actualizada;
  }
}
