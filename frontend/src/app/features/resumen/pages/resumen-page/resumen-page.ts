import { Component, inject } from '@angular/core';
import { FlujoSistema } from '../../../../shared/widgets/flujo-sistema/flujo-sistema';
import { KpiCards } from '../../../../shared/widgets/kpi-cards/kpi-cards';
import { InventarioTabla } from '../../../../shared/widgets/inventario-tabla/inventario-tabla';
import { MovimientosPanel } from '../../../../shared/widgets/movimientos-panel/movimientos-panel';
import { ReposicionPanel } from '../../../../shared/widgets/reposicion-panel/reposicion-panel';
import { ResumenStore } from '../../data/resumen.store';

// la pagina solo compone widgets + su propio store. Cero logica de filtrado
// ni de HTTP aca - todo eso vive en ResumenStore.
@Component({
  selector: 'app-resumen-page',
  imports: [FlujoSistema, KpiCards, InventarioTabla, MovimientosPanel, ReposicionPanel],
  templateUrl: './resumen-page.html',
})
export class ResumenPage {
  // inyectar el store alcanza: su propio effect() dispara la carga inicial
  protected readonly store = inject(ResumenStore);
}
