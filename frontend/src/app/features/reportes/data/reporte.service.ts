import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { Inventario } from '../../inventario/data/inventario.model';
import {
  FiltroMovimientos,
  Movimiento,
  ReporteCumplimientoRecepcion,
  ReporteFinancieroConsolidado,
  ReporteMargenProducto,
  ReporteRotacion,
  ResumenStockTienda,
} from './reporte.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/reportes`;

  stockBajo(): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(`${this.baseUrl}/stock-bajo`);
  }

  resumenPorTienda(): Observable<ResumenStockTienda[]> {
    return this.http.get<ResumenStockTienda[]>(`${this.baseUrl}/resumen-tiendas`);
  }

  movimientos(
    idTiendaOrFiltro: number | null | FiltroMovimientos = null,
  ): Observable<Movimiento[]> {
    let params = new HttpParams();

    if (typeof idTiendaOrFiltro === 'number') {
      params = params.set('idTienda', idTiendaOrFiltro);
    } else if (idTiendaOrFiltro != null) {
      if (idTiendaOrFiltro.idTienda != null) {
        params = params.set('idTienda', idTiendaOrFiltro.idTienda);
      }
      if (idTiendaOrFiltro.idProducto != null) {
        params = params.set('idProducto', idTiendaOrFiltro.idProducto);
      }
      if (idTiendaOrFiltro.page != null) {
        params = params.set('page', idTiendaOrFiltro.page);
      }
      if (idTiendaOrFiltro.size != null) {
        params = params.set('size', idTiendaOrFiltro.size);
      }
    }

    return this.http.get<Movimiento[]>(`${this.baseUrl}/movimientos`, { params });
  }

  margenes(filtros?: {
    idProveedor?: number | null;
    idTienda?: number | null;
  }): Observable<ReporteMargenProducto[]> {
    let params = new HttpParams();
    if (filtros?.idProveedor != null) params = params.set('idProveedor', filtros.idProveedor);
    if (filtros?.idTienda != null) params = params.set('idTienda', filtros.idTienda);
    return this.http.get<ReporteMargenProducto[]>(`${this.baseUrl}/margenes`, { params });
  }

  rotacion(filtros?: {
    idTienda?: number | null;
    idProveedor?: number | null;
    dias?: number | null;
  }): Observable<ReporteRotacion[]> {
    let params = new HttpParams();
    if (filtros?.idTienda != null) params = params.set('idTienda', filtros.idTienda);
    if (filtros?.idProveedor != null) params = params.set('idProveedor', filtros.idProveedor);
    if (filtros?.dias != null) params = params.set('dias', filtros.dias);
    return this.http.get<ReporteRotacion[]>(`${this.baseUrl}/rotacion`, { params });
  }

  cumplimientoRecepcion(filtros?: {
    idProveedor?: number | null;
    idTienda?: number | null;
  }): Observable<ReporteCumplimientoRecepcion[]> {
    let params = new HttpParams();
    if (filtros?.idProveedor != null) params = params.set('idProveedor', filtros.idProveedor);
    if (filtros?.idTienda != null) params = params.set('idTienda', filtros.idTienda);
    return this.http.get<ReporteCumplimientoRecepcion[]>(
      `${this.baseUrl}/cumplimiento-recepcion`,
      { params },
    );
  }

  financieroConsolidado(): Observable<ReporteFinancieroConsolidado> {
    return this.http.get<ReporteFinancieroConsolidado>(`${this.baseUrl}/financiero-consolidado`);
  }
}
