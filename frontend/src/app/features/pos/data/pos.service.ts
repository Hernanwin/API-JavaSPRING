import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { Apartado, ApartadoCrearInput, EstadoApartado } from './pos.model';

@Injectable({ providedIn: 'root' })
export class PosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/apartados`;

  listarApartados(filtros?: {
    idTienda?: number | null;
    estado?: EstadoApartado | null;
  }): Observable<Apartado[]> {
    let params = new HttpParams();
    if (filtros?.idTienda != null) params = params.set('idTienda', filtros.idTienda);
    if (filtros?.estado) params = params.set('estado', filtros.estado);
    return this.http.get<Apartado[]>(this.baseUrl, { params });
  }

  crearApartado(input: ApartadoCrearInput): Observable<Apartado> {
    return this.http.post<Apartado>(this.baseUrl, input);
  }

  cobrarApartado(id: number): Observable<Apartado> {
    return this.http.post<Apartado>(`${this.baseUrl}/${id}/cobrar`, {});
  }

  cancelarApartado(id: number): Observable<Apartado> {
    return this.http.put<Apartado>(`${this.baseUrl}/${id}/cancelar`, {});
  }
}
