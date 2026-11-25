import { Component, input } from '@angular/core';

// widget tonto: no inyecta nada, no sabe que existe un store. Solo pinta
// el paso activo que le pasan por input.
@Component({
  selector: 'app-flujo-sistema',
  imports: [],
  templateUrl: './flujo-sistema.html',
})
export class FlujoSistema {
  readonly activeStep = input<number[]>([2]);

  protected readonly pasos = [
    { numero: 1, codigo: '01', label: 'Producto + Tienda' },
    { numero: 2, codigo: '02', label: 'Inventario' },
    { numero: 3, codigo: '03', label: 'Movimientos' },
  ];
}
