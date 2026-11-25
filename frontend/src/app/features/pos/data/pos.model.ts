export type EstadoApartado = 'ACTIVO' | 'COMPLETADO' | 'CANCELADO';

export interface DetalleApartado {
  id: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Apartado {
  id: number;
  idTienda: number;
  nombreTienda: string;
  clienteNombre: string;
  clienteContacto?: string | null;
  fechaCreacion: string;
  fechaVencimiento: string;
  estado: EstadoApartado;
  total: number;
  montoAbonado: number;
  saldoPendiente: number;
  creadoPor?: string | null;
  idVentaGenerada?: number | null;
  detalle: DetalleApartado[];
}

export interface ApartadoCrearInput {
  idTienda: number;
  clienteNombre: string;
  clienteContacto?: string;
  montoAbonado?: number;
  fechaVencimiento?: string;
  detalle: {
    idProducto: number;
    cantidad: number;
  }[];
}
