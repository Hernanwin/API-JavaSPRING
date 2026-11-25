import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmationService, MessageService } from 'primeng/api';

import { PosStore } from '../../data/pos.store';
import { Apartado, EstadoApartado } from '../../data/pos.model';
import { Venta } from '../../../movimientos/data/operaciones.model';

@Component({
  selector: 'app-pos-page',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    SelectModule,
    InputNumberModule,
    InputTextModule,
    TagModule,
  ],
  templateUrl: './pos-page.html',
})
export class PosPage {
  protected readonly store = inject(PosStore);
  private readonly fb = inject(FormBuilder);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);

  protected readonly ventaVisible = signal(false);
  protected readonly apartadoVisible = signal(false);
  protected readonly editarVentaVisible = signal(false);
  protected readonly ventaEditando = signal<Venta | null>(null);

  protected readonly opcionesEstadoApartado: { label: string; value: EstadoApartado | null }[] = [
    { label: 'Todos los apartados', value: null },
    { label: 'Activos (stock reservado)', value: 'ACTIVO' },
    { label: 'Completados / Cobrados', value: 'COMPLETADO' },
    { label: 'Cancelados', value: 'CANCELADO' },
  ];

  protected readonly estadosVenta = [
    { label: 'COMPLETADA', value: 'COMPLETADA' },
    { label: 'PENDIENTE', value: 'PENDIENTE' },
    { label: 'ENTREGADA', value: 'ENTREGADA' },
    { label: 'CANCELADA', value: 'CANCELADA' },
  ];

  protected readonly formVenta = this.fb.group({
    idTienda: [null as number | null, Validators.required],
    caja: [''],
    detalle: this.fb.array([this.crearLinea()]),
  });

  protected readonly formApartado = this.fb.group({
    idTienda: [null as number | null, Validators.required],
    clienteNombre: ['', Validators.required],
    clienteContacto: [''],
    montoAbonado: [0, [Validators.min(0)]],
    detalle: this.fb.array([this.crearLinea()]),
  });

  protected readonly formEditarVenta = this.fb.group({
    idTienda: [null as number | null, Validators.required],
    estado: ['COMPLETADA', Validators.required],
  });

  protected get lineasVenta(): FormArray {
    return this.formVenta.get('detalle') as FormArray;
  }

  protected get lineasApartado(): FormArray {
    return this.formApartado.get('detalle') as FormArray;
  }

  private crearLinea() {
    return this.fb.group({
      idProducto: [null as number | null, Validators.required],
      cantidad: [1, [Validators.required, Validators.min(1)]],
    });
  }

  protected agregarLinea(arr: FormArray): void {
    arr.push(this.crearLinea());
  }

  protected quitarLinea(arr: FormArray, idx: number): void {
    if (arr.length > 1) arr.removeAt(idx);
  }

  protected totalFormulario(arr: FormArray): number {
    return arr.controls.reduce((acc, c) => {
      const idProd = c.get('idProducto')?.value as number | null;
      const cant = (c.get('cantidad')?.value as number | null) ?? 0;
      return acc + this.store.precioProducto(idProd) * cant;
    }, 0);
  }

  protected abrirNuevaVenta(): void {
    const idSuc = this.store.idSucursalActiva();
    this.lineasVenta.clear();
    this.lineasVenta.push(this.crearLinea());
    this.lineasVenta.at(0).patchValue({
      idProducto: this.store.productos()[0]?.id ?? null,
      cantidad: 1,
    });
    this.formVenta.patchValue({
      idTienda: idSuc,
      caja: this.store.cajaActiva() ?? '',
    });
    this.ventaVisible.set(true);
  }

  protected abrirNuevoApartado(): void {
    const idSuc = this.store.idSucursalActiva();
    this.lineasApartado.clear();
    this.lineasApartado.push(this.crearLinea());
    this.lineasApartado.at(0).patchValue({
      idProducto: this.store.productos()[0]?.id ?? null,
      cantidad: 1,
    });
    this.formApartado.reset({
      idTienda: idSuc,
      clienteNombre: '',
      clienteContacto: '',
      montoAbonado: 0,
    });
    this.lineasApartado.at(0).patchValue({
      idProducto: this.store.productos()[0]?.id ?? null,
      cantidad: 1,
    });
    this.apartadoVisible.set(true);
  }

  protected abrirEditarVenta(v: Venta): void {
    this.ventaEditando.set(v);
    this.formEditarVenta.reset({
      idTienda: v.idTienda,
      estado: v.estado,
    });
    this.editarVentaVisible.set(true);
  }

  protected async confirmarVenta(): Promise<void> {
    if (this.formVenta.invalid) {
      this.formVenta.markAllAsTouched();
      return;
    }
    const v = this.formVenta.getRawValue();
    const creada = await this.store.registrarVenta({
      idTienda: v.idTienda!,
      caja: v.caja || undefined,
      detalle: v.detalle.map((d) => ({
        idProducto: d.idProducto!,
        cantidadProd: d.cantidad!,
      })),
    });
    this.messageService.add({
      severity: 'success',
      summary: `Venta #${creada.id} cobrada`,
      detail: `Total $ ${creada.total.toFixed(2)}${creada.caja ? ' · ' + creada.caja : ''}`,
    });
    this.ventaVisible.set(false);
  }

  protected async confirmarApartado(): Promise<void> {
    if (this.formApartado.invalid) {
      this.formApartado.markAllAsTouched();
      return;
    }
    const v = this.formApartado.getRawValue();
    const creado = await this.store.crearApartado({
      idTienda: v.idTienda!,
      clienteNombre: v.clienteNombre!,
      clienteContacto: v.clienteContacto || undefined,
      montoAbonado: v.montoAbonado ?? 0,
      detalle: v.detalle.map((d) => ({
        idProducto: d.idProducto!,
        cantidad: d.cantidad!,
      })),
    });
    this.messageService.add({
      severity: 'info',
      summary: `Apartado #${creado.id} reservado`,
      detail: `Se apartó la mercadería para ${creado.clienteNombre} (saldo $ ${creado.saldoPendiente.toFixed(2)}).`,
    });
    this.apartadoVisible.set(false);
  }

  protected async cobrarApartado(ap: Apartado): Promise<void> {
    const cobrado = await this.store.cobrarApartado(ap.id);
    this.messageService.add({
      severity: 'success',
      summary: 'Apartado cobrado y entregado',
      detail: `Se generó la Venta #${cobrado.idVentaGenerada} por $ ${cobrado.total.toFixed(2)}.`,
    });
  }

  protected confirmarCancelarApartado(ap: Apartado): void {
    this.confirmationService.confirm({
      header: 'Cancelar apartado y liberar reserva',
      message: `¿Cancelar el Apartado #${ap.id} de ${ap.clienteNombre}? Las unidades volverán a estar disponibles en tienda.`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Cancelar apartado' },
      rejectButtonProps: { severity: 'secondary', label: 'Volver', variant: 'outlined' },
      accept: async () => {
        await this.store.cancelarApartado(ap.id);
        this.messageService.add({
          severity: 'info',
          summary: 'Apartado cancelado',
          detail: 'El stock reservado quedó disponible nuevamente.',
        });
      },
    });
  }

  protected async guardarEdicionVenta(): Promise<void> {
    const venta = this.ventaEditando();
    if (!venta || this.formEditarVenta.invalid) return;
    const v = this.formEditarVenta.getRawValue();
    await this.store.actualizarVenta(venta.id, {
      idTienda: v.idTienda!,
      estado: v.estado!,
    });
    this.editarVentaVisible.set(false);
  }

  protected confirmarEliminarVenta(venta: Venta): void {
    this.confirmationService.confirm({
      header: 'Anular venta y reintegrar stock',
      message: `¿Anular la Venta #${venta.id}? El stock volverá a ${this.store.nombreTienda(venta.idTienda)}.`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Anular venta' },
      rejectButtonProps: { severity: 'secondary', label: 'Cancelar', variant: 'outlined' },
      accept: () => this.store.eliminarVenta(venta.id),
    });
  }
}
