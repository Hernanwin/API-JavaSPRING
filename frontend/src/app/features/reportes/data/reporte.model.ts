export interface ResumenStockTienda {
  idTienda: number;
  nombreTienda: string;
  totalUnidades: number;
  productosDistintos: number;
  valorTotal?: number;
}

export interface FiltroMovimientos {
  idTienda?: number | null;
  idProducto?: number | null;
  page?: number | null;
  size?: number | null;
}

export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'TRANSFERENCIA' | 'AJUSTE_CONTEO';

export interface Movimiento {
  tipo: TipoMovimiento;
  idProducto: number;
  nombreProducto: string;
  idTienda: number;
  nombreTienda: string;
  idTiendaDestino: number | null;
  nombreTiendaDestino: string | null;
  cantidad: number;
  fecha: string;
}

export interface ReporteMargenProducto {
  idProducto: number;
  nombreProducto: string;
  idProveedor: number | null;
  nombreProveedor: string | null;
  precioVentaActual: number;
  costoActual: number;
  margenUnitarioCatalogo: number;
  porcentajeMargenCatalogo: number;
  unidadesVendidas: number;
  ingresosVentas: number;
  costoVentas: number;
  utilidadBruta: number;
  porcentajeMargenRealizado: number;
}

export interface ReporteRotacion {
  idProducto: number;
  nombreProducto: string;
  idProveedor: number | null;
  nombreProveedor: string | null;
  idTienda: number;
  nombreTienda: string;
  stockActual: number;
  unidadesSalidasPeriodo: number;
  indiceRotacion: number;
  diasInventarioEstimados: number | null;
}

export interface ReporteCumplimientoRecepcion {
  idTienda: number;
  nombreTienda: string;
  ordenesCompraTotales: number;
  ordenesCompraCompletas: number;
  ordenesCompraParciales: number;
  ordenesCompraPendientes: number;
  unidadesOcSolicitadas: number;
  unidadesOcRecibidas: number;
  porcentajeCumplimientoOc: number;
  transferenciasCompletadas: number;
  transferenciasEnTransito: number;
  unidadesTransfEnviadas: number;
  unidadesTransfRecibidas: number;
  porcentajeCumplimientoTransferencias: number;
}

export interface ReporteFinancieroConsolidado {
  cantidadVentas: number;
  ingresosTotalesVentas: number;
  costoTotalVentas: number;
  utilidadBrutaVentas: number;
  margenBrutoPorcentaje: number;
  valorInventarioAlCosto: number;
  valorInventarioAPrecioVenta: number;
  apartadosActivosTotal: number;
  apartadosSaldoPendiente: number;
  ordenesCompraComprometidas: number;
}
