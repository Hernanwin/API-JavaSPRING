import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { Notificacion } from './notificacion.model';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/notificaciones`;

  listar(soloNoLeidas = false): Observable<Notificacion[]> {
    const params = new HttpParams().set('soloNoLeidas', soloNoLeidas);
    return this.http.get<Notificacion[]>(this.baseUrl, { params });
  }

  marcarLeida(id: number): Observable<Notificacion> {
    return this.http.put<Notificacion>(`${this.baseUrl}/${id}/leida`, {});
  }

  marcarTodasLeidas(): Observable<{ actualizadas: number }> {
    return this.http.put<{ actualizadas: number }>(`${this.baseUrl}/leidas`, {});
  }

  streamStockBajo(token: string): Observable<Notificacion> {
    return new Observable<Notificacion>((subscriber) => {
      if (typeof EventSource === 'undefined' || !token) {
        return;
      }

      const url = `${this.baseUrl}/stream?token=${encodeURIComponent(token)}`;
      const eventSource = new EventSource(url);

      const handler = (event: MessageEvent) => {
        try {
          const data: Notificacion = JSON.parse(event.data);
          subscriber.next(data);
        } catch {
          // ignorar eventos malformados
        }
      };

      eventSource.addEventListener('stock-bajo', handler as EventListener);

      return () => {
        eventSource.removeEventListener('stock-bajo', handler as EventListener);
        eventSource.close();
      };
    });
  }
}
