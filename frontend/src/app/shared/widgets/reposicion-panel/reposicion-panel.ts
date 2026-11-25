import { Component, input } from '@angular/core';
import { Inventario } from '../../../features/inventario/data/inventario.model';

@Component({
  selector: 'app-reposicion-panel',
  imports: [],
  templateUrl: './reposicion-panel.html',
})
export class ReposicionPanel {
  readonly items = input<Inventario[]>([]);
}
