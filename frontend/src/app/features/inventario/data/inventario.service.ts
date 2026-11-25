import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import {
  AjusteConteo,
  AjusteConteoInput,
  Inventario,
  InventarioConsolidado,
} from './inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/inventario`;

  listarTodo(): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(this.baseUrl);
  }

  listarConsolidado(idProveedor?: number | null): Observable<InventarioConsolidado[]> {
    let params = new HttpParams();
    if (idProveedor != null) params = params.set('idProveedor', idProveedor);
    return this.http.get<InventarioConsolidado[]>(`${this.baseUrl}/consolidado`, { params });
  }

  listarPorProveedor(idProveedor: number): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(`${this.baseUrl}/proveedor/${idProveedor}`);
  }

  listarPorTienda(idTienda: number): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(`${this.baseUrl}/tienda/${idTienda}`);
  }

  listarPorProducto(idProducto: number): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(`${this.baseUrl}/producto/${idProducto}`);
  }

  consultarStock(idProducto: number, idTienda: number): Observable<Inventario> {
    const params = new HttpParams().set('idProducto', idProducto).set('idTienda', idTienda);
    return this.http.get<Inventario>(`${this.baseUrl}/stock`, { params });
  }

  actualizarStockMinimo(
    idProducto: number,
    idTienda: number,
    stockMinimo: number,
  ): Observable<Inventario> {
    const params = new HttpParams()
      .set('idProducto', idProducto)
      .set('idTienda', idTienda)
      .set('stockMinimo', stockMinimo);
    return this.http.put<Inventario>(`${this.baseUrl}/stock-minimo`, null, { params });
  }

  listarAjustesConteo(idTienda?: number | null): Observable<AjusteConteo[]> {
    let params = new HttpParams();
    if (idTienda != null) params = params.set('idTienda', idTienda);
    return this.http.get<AjusteConteo[]>(`${API_BASE_URL}/ajustes-conteo`, { params });
  }

  registrarAjusteConteo(input: AjusteConteoInput): Observable<AjusteConteo> {
    return this.http.post<AjusteConteo>(`${API_BASE_URL}/ajustes-conteo`, input);
  }
}
