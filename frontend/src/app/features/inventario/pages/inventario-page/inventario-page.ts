import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';

import { FlujoSistema } from '../../../../shared/widgets/flujo-sistema/flujo-sistema';
import { KpiCards } from '../../../../shared/widgets/kpi-cards/kpi-cards';
import { InventarioTabla } from '../../../../shared/widgets/inventario-tabla/inventario-tabla';
import { MovimientosPanel } from '../../../../shared/widgets/movimientos-panel/movimientos-panel';
import { ReposicionPanel } from '../../../../shared/widgets/reposicion-panel/reposicion-panel';
import { InventarioStore } from '../../data/inventario.store';
import { Inventario } from '../../data/inventario.model';
import { STOCK_MINIMO_POR_DEFECTO } from '../../../../shared/utils/stock';

type VistaInventario = 'ubicacion' | 'consolidado' | 'conteos';

@Component({
  selector: 'app-inventario-page',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputNumberModule,
    InputTextModule,
    SelectModule,
    TableModule,
    TagModule,
    FlujoSistema,
    KpiCards,
    InventarioTabla,
    MovimientosPanel,
    ReposicionPanel,
  ],
  templateUrl: './inventario-page.html',
})
export class InventarioPage {
  protected readonly store = inject(InventarioStore);
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);

  protected readonly vistaActiva = signal<VistaInventario>('ubicacion');
  protected readonly dialogMinimoVisible = signal(false);
  protected readonly dialogConteoVisible = signal(false);
  protected readonly itemEditando = signal<Inventario | null>(null);

  protected readonly opcionesProveedor = computed(() => [
    { id: null, nombre: 'Todos los proveedores' },
    ...this.store.proveedores().map((p) => ({ id: p.id, nombre: p.nombre })),
  ]);

  protected readonly formMinimo = this.fb.nonNullable.group({
    stockMinimo: [STOCK_MINIMO_POR_DEFECTO, [Validators.required, Validators.min(0)]],
  });

  protected readonly formConteo = this.fb.group({
    idTienda: [null as number | null, Validators.required],
    idProducto: [null as number | null, Validators.required],
    cantidadContada: [0, [Validators.required, Validators.min(0)]],
    motivo: ['CONTEO_CICLICO'],
  });

  protected abrirEdicionMinimo(item: Inventario): void {
    this.itemEditando.set(item);
    this.formMinimo.reset({
      stockMinimo: item.stockMinimo ?? STOCK_MINIMO_POR_DEFECTO,
    });
    this.dialogMinimoVisible.set(true);
  }

  protected abrirNuevoConteo(): void {
    const ubicaciones = this.store.ubicacionesParaConteo();
    const idSeleccionada = this.store.idTiendaSeleccionada();
    const idDefault =
      ubicaciones.find((u) => u.id === idSeleccionada)?.id ?? ubicaciones[0]?.id ?? null;

    this.formConteo.reset({
      idTienda: idDefault,
      idProducto: this.store.productos()[0]?.id ?? null,
      cantidadContada: 0,
      motivo: 'CONTEO_CICLICO',
    });
    this.dialogConteoVisible.set(true);
  }

  protected async guardarMinimo(): Promise<void> {
    const item = this.itemEditando();
    if (!item || this.formMinimo.invalid) {
      this.formMinimo.markAllAsTouched();
      return;
    }

    const { stockMinimo } = this.formMinimo.getRawValue();
    await this.store.actualizarStockMinimo(item.idProducto, item.idTienda, stockMinimo);

    this.messageService.add({
      severity: 'success',
      summary: 'Stock mínimo actualizado',
      detail: `${item.nombreProducto} (${item.nombreTienda}): mínimo fijado en ${stockMinimo}`,
    });

    this.dialogMinimoVisible.set(false);
  }

  protected async guardarConteo(): Promise<void> {
    if (this.formConteo.invalid) {
      this.formConteo.markAllAsTouched();
      return;
    }
    const v = this.formConteo.getRawValue();
    const ajuste = await this.store.registrarAjusteConteo({
      idTienda: v.idTienda!,
      idProducto: v.idProducto!,
      cantidadContada: v.cantidadContada!,
      motivo: v.motivo || 'CONTEO_CICLICO',
    });

    const difTxt = ajuste.diferencia >= 0 ? `+${ajuste.diferencia}` : `${ajuste.diferencia}`;
    this.messageService.add({
      severity: 'success',
      summary: 'Ajuste por conteo aplicado',
      detail: `${ajuste.nombreProducto} en ${ajuste.nombreTienda}: pasó de ${ajuste.cantidadSistemaAnterior} a ${ajuste.cantidadContada} (${difTxt})`,
    });
    this.dialogConteoVisible.set(false);
  }
}
