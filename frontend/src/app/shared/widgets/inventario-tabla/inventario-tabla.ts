import { Component, inject, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';
import { ButtonModule } from 'primeng/button';
import { Inventario } from '../../../features/inventario/data/inventario.model';
import { estaBajoMinimo, nivelPorcentaje, STOCK_MINIMO_POR_DEFECTO } from '../../utils/stock';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  selector: 'app-inventario-tabla',
  imports: [DecimalPipe, TableModule, TagModule, ProgressBarModule, ButtonModule],
  templateUrl: './inventario-tabla.html',
})
export class InventarioTabla {
  protected readonly authStore = inject(AuthStore);

  readonly items = input<Inventario[]>([]);
  readonly cargando = input(false);
  readonly editable = input(false);

  readonly editarMinimo = output<Inventario>();

  protected readonly nivelPorcentaje = nivelPorcentaje;
  protected readonly estaBajo = estaBajoMinimo;
  protected readonly stockMinimoPorDefecto = STOCK_MINIMO_POR_DEFECTO;

  protected puedeConfigurarMinimo(item: Inventario): boolean {
    return (
      this.editable() &&
      this.authStore.tienePermisoEnUbicacion(item.idTienda, 'STOCK_MINIMO_CONFIGURAR')
    );
  }
}
