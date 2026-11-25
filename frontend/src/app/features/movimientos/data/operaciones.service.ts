import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import {
  Entrada,
  EntradaInput,
  EstadoTransferencia,
  Salida,
  SalidaManualInput,
  Transferencia,
  TransferenciaInput,
  Venta,
  VentaActualizarInput,
  VentaCrearInput,
} from './operaciones.model';

@Injectable({ providedIn: 'root' })
export class OperacionesService {
  private readonly http = inject(HttpClient);

  // Entradas (/api/entradas)
  listarEntradas(): Observable<Entrada[]> {
    return this.http.get<Entrada[]>(`${API_BASE_URL}/entradas`);
  }

  crearEntrada(entrada: EntradaInput): Observable<Entrada> {
    return this.http.post<Entrada>(`${API_BASE_URL}/entradas`, entrada);
  }

  // Salidas operativas (/api/salidas)
  listarSalidas(): Observable<Salida[]> {
    return this.http.get<Salida[]>(`${API_BASE_URL}/salidas`);
  }

  crearSalidaManual(salida: SalidaManualInput): Observable<Salida> {
    return this.http.post<Salida>(`${API_BASE_URL}/salidas`, salida);
  }

  // Transferencias y tareas de envio/recepcion (/api/transferencias)
  listarTransferencias(estado?: EstadoTransferencia | null): Observable<Transferencia[]> {
    let params = new HttpParams();
    if (estado) params = params.set('estado', estado);
    return this.http.get<Transferencia[]>(`${API_BASE_URL}/transferencias`, { params });
  }

  crearTransferenciaDirecta(transferencia: TransferenciaInput): Observable<Transferencia> {
    return this.http.post<Transferencia>(`${API_BASE_URL}/transferencias`, transferencia);
  }

  solicitarTransferencia(transferencia: TransferenciaInput): Observable<Transferencia> {
    return this.http.post<Transferencia>(
      `${API_BASE_URL}/transferencias/solicitar`,
      transferencia,
    );
  }

  aprobarTransferencia(id: number): Observable<Transferencia> {
    return this.http.put<Transferencia>(`${API_BASE_URL}/transferencias/${id}/aprobar`, {});
  }

  rechazarTransferencia(id: number, observacion?: string): Observable<Transferencia> {
    let params = new HttpParams();
    if (observacion) params = params.set('observacion', observacion);
    return this.http.put<Transferencia>(`${API_BASE_URL}/transferencias/${id}/rechazar`, null, {
      params,
    });
  }

  enviarTransferencia(id: number): Observable<Transferencia> {
    return this.http.put<Transferencia>(`${API_BASE_URL}/transferencias/${id}/enviar`, {});
  }

  recibirTransferencia(
    id: number,
    cantidadRecibida?: number | null,
    observacion?: string,
  ): Observable<Transferencia> {
    let params = new HttpParams();
    if (cantidadRecibida != null) params = params.set('cantidadRecibida', cantidadRecibida);
    if (observacion) params = params.set('observacion', observacion);
    return this.http.put<Transferencia>(`${API_BASE_URL}/transferencias/${id}/recibir`, null, {
      params,
    });
  }

  // Ventas POS (/api/ventas)
  listarVentas(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${API_BASE_URL}/ventas`);
  }

  crearVenta(venta: VentaCrearInput): Observable<Venta> {
    return this.http.post<Venta>(`${API_BASE_URL}/ventas`, venta);
  }

  actualizarVenta(id: number, venta: VentaActualizarInput): Observable<Venta> {
    return this.http.put<Venta>(`${API_BASE_URL}/ventas/${id}`, venta);
  }

  eliminarVenta(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/ventas/${id}`);
  }
}
