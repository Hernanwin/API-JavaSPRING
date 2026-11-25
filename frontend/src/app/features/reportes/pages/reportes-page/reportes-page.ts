import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';

import { ReportesStore } from '../../data/reportes.store';

type TabReporte = 'margenes' | 'rotacion' | 'cumplimiento' | 'financiero';

@Component({
  selector: 'app-reportes-page',
  imports: [DecimalPipe, FormsModule, TableModule, SelectModule, TagModule, ProgressBarModule],
  templateUrl: './reportes-page.html',
})
export class ReportesPage {
  protected readonly store = inject(ReportesStore);

  protected readonly tabActiva = signal<TabReporte>(
    this.store.puedeMargenesYRotacion() ? 'margenes' : 'financiero',
  );

  protected readonly opcionesDias = [
    { label: 'Últimos 7 días', value: 7 },
    { label: 'Últimos 30 días', value: 30 },
    { label: 'Últimos 90 días', value: 90 },
  ];

  protected readonly opcionesProveedor = computed(() => [
    { id: null, nombre: 'Todos los proveedores' },
    ...this.store.proveedores().map((p) => ({ id: p.id, nombre: p.nombre })),
  ]);
}
