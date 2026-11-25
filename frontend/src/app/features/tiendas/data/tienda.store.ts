import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { TiendaService } from './tienda.service';
import { Tienda, TiendaInput } from './tienda.model';
import { TiendaSeleccionadaStore } from '../../../core/state/tienda-seleccionada.store';

// mismo patron exacto que ProductoStore: service adentro, estado en
// signals privados, solo lectura hacia afuera.
@Injectable({ providedIn: 'root' })
export class TiendaStore {
  private readonly service = inject(TiendaService);
  private readonly seleccion = inject(TiendaSeleccionadaStore);

  private readonly _tiendas = signal<Tienda[]>([]);
  private readonly _cargando = signal(false);

  readonly tiendas = this._tiendas.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  async cargar(): Promise<void> {
    this._cargando.set(true);
    try {
      const tiendas = await firstValueFrom(this.service.listar());
      this._tiendas.set(tiendas);
    } finally {
      this._cargando.set(false);
    }
  }

  async crear(tienda: TiendaInput): Promise<void> {
    const creada = await firstValueFrom(this.service.crear(tienda));
    this._tiendas.update((actuales) => [...actuales, creada]);
  }

  async actualizar(id: number, tienda: TiendaInput): Promise<void> {
    const actualizada = await firstValueFrom(this.service.actualizar(id, tienda));
    this._tiendas.update((actuales) => actuales.map((t) => (t.id === id ? actualizada : t)));
  }

  async eliminar(id: number): Promise<void> {
    await firstValueFrom(this.service.eliminar(id));
    this._tiendas.update((actuales) => actuales.filter((t) => t.id !== id));
    if (this.seleccion.idTienda() === id) {
      this.seleccion.seleccionar(null);
    }
  }
}
