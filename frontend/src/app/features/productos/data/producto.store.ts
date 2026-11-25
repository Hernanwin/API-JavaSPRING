import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ProductoService } from './producto.service';
import { Producto, ProductoInput } from './producto.model';

// capa 2: el estado en si. Es el UNICO lugar que guarda la lista de productos
// en memoria. Las paginas nunca llaman al service directo - siempre pasan por aca,
// asi si mañana dos pantallas distintas necesitan la lista de productos,
// las dos ven el mismo estado sin duplicar el fetch.
@Injectable({ providedIn: 'root' })
export class ProductoStore {
  private readonly service = inject(ProductoService);

  // privado: nadie de afuera puede hacer _productos.set(...) a mano
  private readonly _productos = signal<Producto[]>([]);
  private readonly _cargando = signal(false);

  // publico: de solo lectura. Las paginas leen esto, nunca lo escriben.
  readonly productos = this._productos.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  async cargar(): Promise<void> {
    this._cargando.set(true);
    try {
      const productos = await firstValueFrom(this.service.listar());
      this._productos.set(productos);
    } finally {
      this._cargando.set(false);
    }
  }

  async crear(producto: ProductoInput): Promise<void> {
    const creado = await firstValueFrom(this.service.crear(producto));
    this._productos.update((actuales) => [...actuales, creado]);
  }

  async actualizar(id: number, producto: ProductoInput): Promise<void> {
    const actualizado = await firstValueFrom(this.service.actualizar(id, producto));
    this._productos.update((actuales) => actuales.map((p) => (p.id === id ? actualizado : p)));
  }

  async eliminar(id: number): Promise<void> {
    await firstValueFrom(this.service.eliminar(id));
    this._productos.update((actuales) => actuales.filter((p) => p.id !== id));
  }
}
