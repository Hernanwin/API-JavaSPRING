import { Injectable, signal } from '@angular/core';

// estado global de UI: que tienda esta elegida en el selector de arriba a la
// derecha. null = "Todas las tiendas". Vive en core/ (no en features/tiendas)
// porque lo leen Resumen, Inventario y Movimientos - no es dato de Tienda en si,
// es una preferencia de vista compartida entre features.
@Injectable({ providedIn: 'root' })
export class TiendaSeleccionadaStore {
  private readonly _idTienda = signal<number | null>(null);

  readonly idTienda = this._idTienda.asReadonly();

  seleccionar(idTienda: number | null): void {
    this._idTienda.set(idTienda);
  }
}
