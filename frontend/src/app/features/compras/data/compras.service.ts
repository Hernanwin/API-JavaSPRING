import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import {
  EstadoOrdenCompra,
  OrdenCompra,
  OrdenCompraInput,
  Proveedor,
  ProveedorInput,
  RecepcionOrdenCompraInput,
} from './compras.model';

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly http = inject(HttpClient);

  // Proveedores (/api/proveedores)
  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${API_BASE_URL}/proveedores`);
  }

  crearProveedor(input: ProveedorInput): Observable<Proveedor> {
    return this.http.post<Proveedor>(`${API_BASE_URL}/proveedores`, input);
  }

  actualizarProveedor(id: number, input: ProveedorInput): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${API_BASE_URL}/proveedores/${id}`, input);
  }

  eliminarProveedor(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/proveedores/${id}`);
  }

  // Ordenes de compra (/api/ordenes-compra)
  listarOrdenes(filtros?: {
    estado?: EstadoOrdenCompra | null;
    idProveedor?: number | null;
    idTiendaDestino?: number | null;
  }): Observable<OrdenCompra[]> {
    let params = new HttpParams();
    if (filtros?.estado) params = params.set('estado', filtros.estado);
    if (filtros?.idProveedor != null) params = params.set('idProveedor', filtros.idProveedor);
    if (filtros?.idTiendaDestino != null) params = params.set('idTiendaDestino', filtros.idTiendaDestino);
    return this.http.get<OrdenCompra[]>(`${API_BASE_URL}/ordenes-compra`, { params });
  }

  crearOrden(input: OrdenCompraInput): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`${API_BASE_URL}/ordenes-compra`, input);
  }

  aprobarOrden(id: number): Observable<OrdenCompra> {
    return this.http.put<OrdenCompra>(`${API_BASE_URL}/ordenes-compra/${id}/aprobar`, {});
  }

  cancelarOrden(id: number): Observable<OrdenCompra> {
    return this.http.put<OrdenCompra>(`${API_BASE_URL}/ordenes-compra/${id}/cancelar`, {});
  }

  registrarRecepcion(id: number, recepcion: RecepcionOrdenCompraInput): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(
      `${API_BASE_URL}/ordenes-compra/${id}/recepcion`,
      recepcion,
    );
  }
}
