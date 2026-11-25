import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-kpi-cards',
  imports: [DecimalPipe],
  templateUrl: './kpi-cards.html',
})
export class KpiCards {
  readonly unidadesEnStock = input.required<number>();
  readonly etiquetaAlcance = input.required<string>();
  readonly valorInventario = input<number | null>(null);
  readonly etiquetaValor = input<string>('Valor a precio venta');
  readonly alertasStockBajo = input.required<number>();
  readonly ventasHoy = input.required<number>();
  readonly variacionVentas = input<number | null>(null);
}
