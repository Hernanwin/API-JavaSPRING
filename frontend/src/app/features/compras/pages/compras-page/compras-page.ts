import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';
import { ConfirmationService, MessageService } from 'primeng/api';

import { ComprasStore } from '../../data/compras.store';
import { EstadoOrdenCompra, OrdenCompra, Proveedor } from '../../data/compras.model';

@Component({
  selector: 'app-compras-page',
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
    ProgressBarModule,
  ],
  templateUrl: './compras-page.html',
})
export class ComprasPage {
  protected readonly store = inject(ComprasStore);
  private readonly fb = inject(FormBuilder);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);

  protected readonly vistaActiva = signal<'ordenes' | 'proveedores'>('ordenes');
  protected readonly dialogOcVisible = signal(false);
  protected readonly dialogProveedorVisible = signal(false);
  protected readonly dialogRecepcionVisible = signal(false);
  protected readonly proveedorEditando = signal<Proveedor | null>(null);
  protected readonly ordenRecibiendo = signal<OrdenCompra | null>(null);

  protected readonly opcionesEstado: { label: string; value: EstadoOrdenCompra | null }[] = [
    { label: 'Todos los estados', value: null },
    { label: 'Pendientes de aprobación', value: 'PENDIENTE_APROBACION' },
    { label: 'Aprobadas (en espera de entrega)', value: 'APROBADA' },
    { label: 'Recepción parcial', value: 'RECIBIDA_PARCIAL' },
    { label: 'Recepción completa', value: 'RECIBIDA_COMPLETA' },
    { label: 'Canceladas', value: 'CANCELADA' },
  ];

  protected readonly opcionesFiltroProv = computed(() => [
    { id: null, nombre: 'Todos los proveedores' },
    ...this.store.proveedores().map((p) => ({ id: p.id, nombre: p.nombre })),
  ]);

  protected readonly formProveedor = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    identificacionFiscal: [''],
    contacto: [''],
    email: [''],
    telefono: [''],
  });

  protected readonly formOrden = this.fb.group({
    idProveedor: [null as number | null, Validators.required],
    idTiendaDestino: [null as number | null, Validators.required],
    observacion: [''],
    detalle: this.fb.array([this.crearLineaOc()]),
  });

  protected readonly formRecepcion = this.fb.group({
    observacion: [''],
    items: this.fb.array([]),
  });

  protected get lineasOc(): FormArray {
    return this.formOrden.get('detalle') as FormArray;
  }

  protected get lineasRecepcion(): FormArray {
    return this.formRecepcion.get('items') as FormArray;
  }

  private crearLineaOc() {
    return this.fb.group({
      idProducto: [null as number | null, Validators.required],
      cantidadSolicitada: [10, [Validators.required, Validators.min(1)]],
      costoUnitario: [0, [Validators.required, Validators.min(0)]],
    });
  }

  protected agregarLineaOc(): void {
    this.lineasOc.push(this.crearLineaOc());
  }

  protected quitarLineaOc(idx: number): void {
    if (this.lineasOc.length > 1) this.lineasOc.removeAt(idx);
  }

  protected abrirNuevaOrden(): void {
    const prod = this.store.productos()[0];
    this.lineasOc.clear();
    this.lineasOc.push(this.crearLineaOc());
    this.lineasOc.at(0).patchValue({
      idProducto: prod?.id ?? null,
      cantidadSolicitada: 10,
      costoUnitario: prod?.costo ?? 0,
    });
    this.formOrden.patchValue({
      idProveedor: this.store.proveedores()[0]?.id ?? null,
      idTiendaDestino: this.store.destinos()[0]?.id ?? null,
      observacion: '',
    });
    this.dialogOcVisible.set(true);
  }

  protected abrirNuevoProveedor(): void {
    this.proveedorEditando.set(null);
    this.formProveedor.reset({
      nombre: '',
      identificacionFiscal: '',
      contacto: '',
      email: '',
      telefono: '',
    });
    this.dialogProveedorVisible.set(true);
  }

  protected abrirEditarProveedor(p: Proveedor): void {
    this.proveedorEditando.set(p);
    this.formProveedor.reset({
      nombre: p.nombre,
      identificacionFiscal: p.identificacionFiscal ?? '',
      contacto: p.contacto ?? '',
      email: p.email ?? '',
      telefono: p.telefono ?? '',
    });
    this.dialogProveedorVisible.set(true);
  }

  protected abrirRecepcion(oc: OrdenCompra): void {
    this.ordenRecibiendo.set(oc);
    this.lineasRecepcion.clear();
    for (const det of oc.detalle) {
      const pendiente = Math.max(0, det.cantidadSolicitada - det.cantidadRecibida);
      if (pendiente > 0) {
        this.lineasRecepcion.push(
          this.fb.group({
            idProducto: [det.idProducto],
            nombreProducto: [det.nombreProducto],
            pendiente: [pendiente],
            cantidadRecibida: [pendiente, [Validators.required, Validators.min(0), Validators.max(pendiente)]],
          }),
        );
      }
    }
    this.formRecepcion.patchValue({ observacion: '' });
    this.dialogRecepcionVisible.set(true);
  }

  protected async guardarOrden(): Promise<void> {
    if (this.formOrden.invalid) {
      this.formOrden.markAllAsTouched();
      return;
    }
    const v = this.formOrden.getRawValue();
    const creada = await this.store.crearOrden({
      idProveedor: v.idProveedor!,
      idTiendaDestino: v.idTiendaDestino!,
      observacion: v.observacion || undefined,
      detalle: v.detalle.map((d) => ({
        idProducto: d.idProducto!,
        cantidadSolicitada: d.cantidadSolicitada!,
        costoUnitario: d.costoUnitario ?? undefined,
      })),
    });
    this.messageService.add({
      severity: 'info',
      summary: `OC #${creada.id} generada`,
      detail: 'Quedó en estado PENDIENTE_APROBACION.',
    });
    this.dialogOcVisible.set(false);
  }

  protected async aprobar(oc: OrdenCompra): Promise<void> {
    await this.store.aprobarOrden(oc.id);
    this.messageService.add({
      severity: 'success',
      summary: `OC #${oc.id} aprobada`,
      detail: `Habilitada para recepción en ${oc.nombreTiendaDestino}.`,
    });
  }

  protected confirmarCancelar(oc: OrdenCompra): void {
    this.confirmationService.confirm({
      header: 'Cancelar orden de compra',
      message: `¿Cancelar la Orden de Compra #${oc.id} a ${oc.nombreProveedor}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Cancelar OC' },
      rejectButtonProps: { severity: 'secondary', label: 'Volver', variant: 'outlined' },
      accept: () => this.store.cancelarOrden(oc.id),
    });
  }

  protected async confirmarRecepcionOc(): Promise<void> {
    const oc = this.ordenRecibiendo();
    if (!oc || this.formRecepcion.invalid) return;
    const raw = this.formRecepcion.getRawValue();
    const items = (raw.items as { idProducto: number; cantidadRecibida: number }[])
      .filter((i) => i.cantidadRecibida > 0)
      .map((i) => ({ idProducto: i.idProducto, cantidadRecibida: i.cantidadRecibida }));

    const res = await this.store.registrarRecepcion(oc.id, {
      items,
      observacion: raw.observacion || undefined,
    });
    this.messageService.add({
      severity: 'success',
      summary: 'Mercadería recibida',
      detail: `OC #${res.id}: cumplimiento ${res.porcentajeCumplimiento}% en ${res.nombreTiendaDestino}.`,
    });
    this.dialogRecepcionVisible.set(false);
  }

  protected async guardarProveedor(): Promise<void> {
    if (this.formProveedor.invalid) {
      this.formProveedor.markAllAsTouched();
      return;
    }
    const v = this.formProveedor.getRawValue();
    const ed = this.proveedorEditando();
    if (ed) {
      await this.store.actualizarProveedor(ed.id, v);
    } else {
      await this.store.crearProveedor(v);
    }
    this.dialogProveedorVisible.set(false);
  }

  protected confirmarEliminarProveedor(p: Proveedor): void {
    this.confirmationService.confirm({
      header: 'Eliminar proveedor',
      message: `¿Seguro que querés eliminar a "${p.nombre}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Eliminar' },
      rejectButtonProps: { severity: 'secondary', label: 'Cancelar', variant: 'outlined' },
      accept: () => this.store.eliminarProveedor(p.id),
    });
  }

  protected severidadEstado(
    e: EstadoOrdenCompra,
  ): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (e) {
      case 'RECIBIDA_COMPLETA':
        return 'success';
      case 'RECIBIDA_PARCIAL':
        return 'info';
      case 'APROBADA':
        return 'warn';
      case 'CANCELADA':
        return 'danger';
      default:
        return 'secondary';
    }
  }
}
