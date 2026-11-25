import { Component, input } from '@angular/core';
import { Movimiento, TipoMovimiento } from '../../../features/reportes/data/reporte.model';

@Component({
  selector: 'app-movimientos-panel',
  imports: [],
  templateUrl: './movimientos-panel.html',
})
export class MovimientosPanel {
  readonly items = input<Movimiento[]>([]);

  protected etiqueta(tipo: TipoMovimiento): string {
    switch (tipo) {
      case 'SALIDA':
        return 'Salida / Venta';
      case 'ENTRADA':
        return 'Entrada';
      case 'AJUSTE_CONTEO':
        return 'Ajuste Conteo';
      default:
        return 'Transferencia';
    }
  }

  protected icono(tipo: TipoMovimiento): string {
    switch (tipo) {
      case 'SALIDA':
        return 'pi pi-arrow-up';
      case 'ENTRADA':
        return 'pi pi-arrow-down';
      case 'AJUSTE_CONTEO':
        return 'pi pi-check-square';
      default:
        return 'pi pi-arrow-right-arrow-left';
    }
  }

  protected color(tipo: TipoMovimiento): string {
    switch (tipo) {
      case 'SALIDA':
        return 'text-yellow-400 bg-yellow-400/10';
      case 'ENTRADA':
        return 'text-green-400 bg-green-400/10';
      case 'AJUSTE_CONTEO':
        return 'text-purple-400 bg-purple-400/10';
      default:
        return 'text-blue-400 bg-blue-400/10';
    }
  }

  protected signo(m: Movimiento): string {
    if (m.tipo === 'ENTRADA') return '+';
    if (m.tipo === 'SALIDA') return '-';
    if (m.tipo === 'AJUSTE_CONTEO') return m.cantidad >= 0 ? '+' : '';
    return '↔ ';
  }
}
