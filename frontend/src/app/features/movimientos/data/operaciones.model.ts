export interface Entrada {
  id: number;
  idProducto: number;
  nombreProducto: string;
  idTienda: number;
  nombreTienda: string;
  cantidad: number;
  fecha: string;
  observacion: string | null;
}

export interface EntradaInput {
  idProducto: number;
  idTienda: number;
  cantidad: number;
  observacion?: string;
}

export interface Salida {
  id: number;
  idProducto: number;
  nombreProducto: string;
  idTienda: number;
  nombreTienda: string;
  idVenta: number | null;
  cantidad: number;
  fecha: string;
  motivo?: string;
  observacion?: string;
  registradoPor?: string;
}

export interface SalidaManualInput {
  idProducto: number;
  idTienda: number;
  cantidad: number;
  motivo: string;
  observacion?: string;
}

export type EstadoTransferencia =
  | 'SOLICITADA'
  | 'APROBADA'
  | 'EN_TRANSITO'
  | 'COMPLETADA'
  | 'RECHAZADA';

export interface Transferencia {
  id: number;
  idProducto: number;
  nombreProducto: string;
  idTiendaOrigen: number;
  nombreTiendaOrigen: string;
  idTiendaDestino: number;
  nombreTiendaDestino: string;
  cantidad: number;
  cantidadEnviada?: number | null;
  cantidadRecibida?: number | null;
  estado: EstadoTransferencia;
  fecha: string;
  fechaAprobacion?: string | null;
  fechaEnvio?: string | null;
  fechaRecepcion?: string | null;
  solicitadoPor?: string | null;
  aprobadoPor?: string | null;
  enviadoPor?: string | null;
  recibidoPor?: string | null;
  observacion?: string | null;
}

export interface TransferenciaInput {
  idProducto: number;
  idTiendaOrigen: number;
  idTiendaDestino: number;
  cantidad: number;
  observacion?: string;
}

export interface DetalleVenta {
  id: number;
  idProducto: number;
  nombreProd: string;
  cantidadProd: number;
  precioUnitario: number;
  subtotal: number;
}

export interface DetalleVentaInput {
  idProducto: number;
  cantidadProd: number;
}

export interface Venta {
  id: number;
  fechaHora: string;
  estado: string;
  caja?: string | null;
  cajeroUsername?: string | null;
  idTienda: number;
  detalle: DetalleVenta[];
  total: number;
}

export interface VentaCrearInput {
  idTienda: number;
  estado?: string;
  caja?: string;
  detalle: DetalleVentaInput[];
}

export interface VentaActualizarInput {
  idTienda?: number;
  estado?: string;
  fechaHora?: string;
}
