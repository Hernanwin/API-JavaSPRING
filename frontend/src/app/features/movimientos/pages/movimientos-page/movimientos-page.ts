import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';

import { FlujoSistema } from '../../../../shared/widgets/flujo-sistema/flujo-sistema';
import { KpiCards } from '../../../../shared/widgets/kpi-cards/kpi-cards';
import { ReposicionPanel } from '../../../../shared/widgets/reposicion-panel/reposicion-panel';
import { MovimientosStore } from '../../data/movimientos.store';
import { EstadoTransferencia, Transferencia } from '../../data/operaciones.model';
import { TipoMovimiento } from '../../../reportes/data/reporte.model';

@Component({
  selector: 'app-movimientos-page',
  imports: [
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    SelectModule,
    InputNumberModule,
    InputTextModule,
    TagModule,
    FlujoSistema,
    KpiCards,
    ReposicionPanel,
  ],
  templateUrl: './movimientos-page.html',
})
export class MovimientosPage {
  protected readonly store = inject(MovimientosStore);
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);

  protected readonly entradaVisible = signal(false);
  protected readonly salidaVisible = signal(false);
  protected readonly transferenciaVisible = signal(false);
  protected readonly recepcionVisible = signal(false);
  protected readonly transferenciaRecibiendo = signal<Transferencia | null>(null);

  protected readonly motivosSalida = [
    { label: 'Merma operativa', value: 'MERMA' },
    { label: 'Rotura / Deterioro', value: 'ROTURA' },
    { label: 'Consumo interno', value: 'CONSUMO_INTERNO' },
    { label: 'Despacho logístico externo', value: 'DESPACHO_EXTERNO' },
  ];

  protected readonly opcionesEstadoTransferencia: {
    label: string;
    value: EstadoTransferencia | null;
  }[] = [
    { label: 'Todos los estados', value: null },
    { label: 'Solicitadas (pendientes aprobación)', value: 'SOLICITADA' },
    { label: 'Aprobadas (listas para despacho)', value: 'APROBADA' },
    { label: 'En tránsito (pendientes recepción)', value: 'EN_TRANSITO' },
    { label: 'Completadas', value: 'COMPLETADA' },
    { label: 'Rechazadas', value: 'RECHAZADA' },
  ];

  protected readonly tamanosPagina = [
    { label: '10 por página', value: 10 },
    { label: '25 por página', value: 25 },
    { label: '50 por página', value: 50 },
  ];

  protected readonly opcionesFiltroProducto = () => [
    { id: null, nombre: 'Todos los productos' },
    ...this.store.productos().map((p) => ({ id: p.id, nombre: p.nombre })),
  ];

  protected readonly formEntrada = this.fb.group({
    idProducto: [null as number | null, Validators.required],
    idTienda: [null as number | null, Validators.required],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    observacion: [''],
  });

  protected readonly formSalida = this.fb.group({
    idProducto: [null as number | null, Validators.required],
    idTienda: [null as number | null, Validators.required],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    motivo: ['MERMA', Validators.required],
    observacion: [''],
  });

  protected readonly formTransferencia = this.fb.group({
    idProducto: [null as number | null, Validators.required],
    idTiendaOrigen: [null as number | null, Validators.required],
    idTiendaDestino: [null as number | null, Validators.required],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    observacion: [''],
  });

  protected readonly formRecepcion = this.fb.group({
    cantidadRecibida: [1, [Validators.required, Validators.min(0)]],
    observacion: [''],
  });

  protected abrirEntrada(): void {
    const ubicaciones = this.store.ubicacionesEntrada();
    const sel = this.store.idTiendaSeleccionada();
    this.formEntrada.reset({
      idProducto: this.store.productos()[0]?.id ?? null,
      idTienda: ubicaciones.find((u) => u.id === sel)?.id ?? ubicaciones[0]?.id ?? null,
      cantidad: 1,
      observacion: '',
    });
    this.entradaVisible.set(true);
  }

  protected abrirSalida(): void {
    const ubicaciones = this.store.ubicacionesSalidaManual();
    const sel = this.store.idTiendaSeleccionada();
    this.formSalida.reset({
      idProducto: this.store.productos()[0]?.id ?? null,
      idTienda: ubicaciones.find((u) => u.id === sel)?.id ?? ubicaciones[0]?.id ?? null,
      cantidad: 1,
      motivo: 'MERMA',
      observacion: '',
    });
    this.salidaVisible.set(true);
  }

  protected abrirTransferencia(): void {
    const destinos = this.store.catalogoDestinos();
    const propias = this.store.tiendas();
    const origen = this.store.idTiendaSeleccionada() ?? propias[0]?.id ?? destinos[0]?.id ?? null;
    const destino = destinos.find((t) => t.id !== origen)?.id ?? null;

    this.formTransferencia.reset({
      idProducto: this.store.productos()[0]?.id ?? null,
      idTiendaOrigen: origen,
      idTiendaDestino: destino,
      cantidad: 1,
      observacion: '',
    });
    this.transferenciaVisible.set(true);
  }

  protected abrirRecepcion(t: Transferencia): void {
    this.transferenciaRecibiendo.set(t);
    this.formRecepcion.reset({
      cantidadRecibida: t.cantidadEnviada ?? t.cantidad,
      observacion: '',
    });
    this.recepcionVisible.set(true);
  }

  protected async guardarEntrada(): Promise<void> {
    if (this.formEntrada.invalid) {
      this.formEntrada.markAllAsTouched();
      return;
    }
    const v = this.formEntrada.getRawValue();
    await this.store.registrarEntrada({
      idProducto: v.idProducto!,
      idTienda: v.idTienda!,
      cantidad: v.cantidad!,
      observacion: v.observacion || undefined,
    });
    this.messageService.add({
      severity: 'success',
      summary: 'Entrada registrada',
      detail: `Se sumaron ${v.cantidad} unidades al stock.`,
    });
    this.entradaVisible.set(false);
  }

  protected async guardarSalida(): Promise<void> {
    if (this.formSalida.invalid) {
      this.formSalida.markAllAsTouched();
      return;
    }
    const v = this.formSalida.getRawValue();
    await this.store.registrarSalidaManual({
      idProducto: v.idProducto!,
      idTienda: v.idTienda!,
      cantidad: v.cantidad!,
      motivo: v.motivo!,
      observacion: v.observacion || undefined,
    });
    this.messageService.add({
      severity: 'success',
      summary: 'Salida operativa registrada',
      detail: `Se descontaron ${v.cantidad} unidades (${v.motivo}).`,
    });
    this.salidaVisible.set(false);
  }

  protected async guardarSolicitudTransferencia(): Promise<void> {
    if (this.formTransferencia.invalid) {
      this.formTransferencia.markAllAsTouched();
      return;
    }
    const v = this.formTransferencia.getRawValue();
    await this.store.solicitarTransferencia({
      idProducto: v.idProducto!,
      idTiendaOrigen: v.idTiendaOrigen!,
      idTiendaDestino: v.idTiendaDestino!,
      cantidad: v.cantidad!,
      observacion: v.observacion || undefined,
    });
    this.messageService.add({
      severity: 'info',
      summary: 'Transferencia solicitada',
      detail: 'Quedó pendiente de aprobación por el Supervisor de Compras o Administrador.',
    });
    this.transferenciaVisible.set(false);
  }

  protected async aprobar(t: Transferencia): Promise<void> {
    await this.store.aprobarTransferencia(t.id);
    this.messageService.add({
      severity: 'success',
      summary: 'Transferencia aprobada',
      detail: `Transferencia #${t.id} habilitada para despacho en ${t.nombreTiendaOrigen}.`,
    });
  }

  protected async rechazar(t: Transferencia): Promise<void> {
    await this.store.rechazarTransferencia(t.id);
    this.messageService.add({
      severity: 'warn',
      summary: 'Transferencia rechazada',
      detail: `La solicitud #${t.id} fue rechazada sin afectar stock.`,
    });
  }

  protected async despachar(t: Transferencia): Promise<void> {
    await this.store.enviarTransferencia(t.id);
    this.messageService.add({
      severity: 'info',
      summary: 'Mercadería despachada',
      detail: `Se descontó el stock en ${t.nombreTiendaOrigen}; envío en tránsito hacia ${t.nombreTiendaDestino}.`,
    });
  }

  protected async confirmarRecepcion(): Promise<void> {
    const t = this.transferenciaRecibiendo();
    if (!t || this.formRecepcion.invalid) {
      this.formRecepcion.markAllAsTouched();
      return;
    }
    const v = this.formRecepcion.getRawValue();
    await this.store.recibirTransferencia(t.id, v.cantidadRecibida!, v.observacion || undefined);
    this.messageService.add({
      severity: 'success',
      summary: 'Recepción completada',
      detail: `Ingresaron ${v.cantidadRecibida} unidades en ${t.nombreTiendaDestino}.`,
    });
    this.recepcionVisible.set(false);
  }

  protected severidadEstadoTransf(
    e: EstadoTransferencia,
  ): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (e) {
      case 'COMPLETADA':
        return 'success';
      case 'EN_TRANSITO':
        return 'info';
      case 'APROBADA':
        return 'warn';
      case 'RECHAZADA':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  protected severidadTipo(tipo: TipoMovimiento): 'success' | 'warn' | 'info' | 'secondary' {
    if (tipo === 'ENTRADA') return 'success';
    if (tipo === 'SALIDA') return 'warn';
    if (tipo === 'AJUSTE_CONTEO') return 'secondary';
    return 'info';
  }
}
