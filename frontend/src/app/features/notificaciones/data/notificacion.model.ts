// espejo de NotificacionDTO del backend (/api/notificaciones)
export interface Notificacion {
  id: number;
  tipo: string; // 'STOCK_BAJO'
  idProducto: number;
  nombreProducto: string;
  idTienda: number;
  nombreTienda: string;
  cantidadActual: number;
  stockMinimo: number;
  mensaje: string;
  fecha: string; // ISO LocalDateTime
  leida: boolean;
}
