export interface Proveedor {
  id: number;
  nombre: string;
  identificacionFiscal: string | null;
  contacto: string | null;
  email: string | null;
  telefono: string | null;
  activo: boolean;
}

export interface ProveedorInput {
  nombre: string;
  identificacionFiscal?: string;
  contacto?: string;
  email?: string;
  telefono?: string;
  activo?: boolean;
}

export type EstadoOrdenCompra =
  | 'PENDIENTE_APROBACION'
  | 'APROBADA'
  | 'RECIBIDA_PARCIAL'
  | 'RECIBIDA_COMPLETA'
  | 'CANCELADA';

export interface DetalleOrdenCompra {
  id: number;
  idProducto: number;
  nombreProducto: string;
  cantidadSolicitada: number;
  cantidadRecibida: number;
  costoUnitario?: number;
  subtotal?: number;
}

export interface OrdenCompra {
  id: number;
  idProveedor: number;
  nombreProveedor: string;
  idTiendaDestino: number;
  nombreTiendaDestino: string;
  estado: EstadoOrdenCompra;
  fechaCreacion: string;
  fechaAprobacion?: string;
  fechaUltimaRecepcion?: string;
  totalEstimado?: number;
  totalUnidadesSolicitadas: number;
  totalUnidadesRecibidas: number;
  porcentajeCumplimiento: number;
  creadoPor?: string;
  aprobadoPor?: string;
  observacion?: string;
  detalle: DetalleOrdenCompra[];
}

export interface OrdenCompraInput {
  idProveedor: number;
  idTiendaDestino: number;
  observacion?: string;
  detalle: {
    idProducto: number;
    cantidadSolicitada: number;
    costoUnitario?: number;
  }[];
}

export interface RecepcionOrdenCompraInput {
  items?: {
    idProducto: number;
    cantidadRecibida: number;
  }[];
  observacion?: string;
}
