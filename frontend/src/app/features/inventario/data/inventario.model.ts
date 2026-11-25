import { TipoUbicacion } from '../../../core/auth/auth.model';

export interface Inventario {
  id: number;
  idProducto: number;
  nombreProducto: string;
  idProveedor?: number | null;
  nombreProveedor?: string | null;
  idTienda: number;
  nombreTienda: string;
  tipoUbicacion?: TipoUbicacion | null;
  cantidad: number;
  cantidadReservada?: number;
  cantidadDisponible?: number;
  stockMinimo: number | null;
  precioVenta?: number | null;
  costoUnitario?: number | null;
  valorCostoTotal?: number | null;
}

export interface InventarioConsolidado {
  idProducto: number;
  nombreProducto: string;
  categoria: string;
  idProveedor?: number | null;
  nombreProveedor?: string | null;
  totalUnidades: number;
  totalReservadas: number;
  totalDisponibles: number;
  ubicacionesConStock: number;
  precioVenta?: number | null;
  costoUnitario?: number | null;
  valorTotalCosto?: number | null;
}

export interface AjusteConteo {
  id: number;
  idProducto: number;
  nombreProducto: string;
  idTienda: number;
  nombreTienda: string;
  cantidadSistemaAnterior: number;
  cantidadContada: number;
  diferencia: number;
  motivo: string;
  ejecutadoPor: string;
  fecha: string;
}

export interface AjusteConteoInput {
  idProducto: number;
  idTienda: number;
  cantidadContada: number;
  motivo?: string;
}
