import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { Tienda, TiendaInput } from './tienda.model';

@Injectable({ providedIn: 'root' })
export class TiendaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/tiendas`;

  // Devuelve solo las ubicaciones dentro del alcance asignado al usuario (o todas si es GLOBAL)
  listar(): Observable<Tienda[]> {
    return this.http.get<Tienda[]>(this.baseUrl);
  }

  // Directorio completo para elegir origen/destino en solicitudes de transferencia u ordenes de compra
  listarCatalogoDestinos(): Observable<Tienda[]> {
    return this.http.get<Tienda[]>(`${this.baseUrl}/catalogo`);
  }

  crear(tienda: TiendaInput): Observable<Tienda> {
    return this.http.post<Tienda>(this.baseUrl, tienda);
  }

  actualizar(id: number, tienda: TiendaInput): Observable<Tienda> {
    return this.http.put<Tienda>(`${this.baseUrl}/${id}`, tienda);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
