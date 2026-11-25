import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ConfirmationService } from 'primeng/api';

import { ProductoStore } from '../../data/producto.store';
import { Producto } from '../../data/producto.model';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ComprasService } from '../../../compras/data/compras.service';
import { Proveedor } from '../../../compras/data/compras.model';

@Component({
  selector: 'app-productos-page',
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    TagModule,
  ],
  templateUrl: './productos-page.html',
})
export class ProductosPage {
  protected readonly store = inject(ProductoStore);
  protected readonly authStore = inject(AuthStore);
  private readonly comprasService = inject(ComprasService);
  private readonly fb = inject(FormBuilder);
  private readonly confirmationService = inject(ConfirmationService);

  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly dialogVisible = signal(false);
  protected readonly editando = signal<Producto | null>(null);

  protected readonly puedeGestionar = computed(
    () =>
      this.authStore.tienePermiso('CATALOGO_GESTIONAR') &&
      this.authStore.tienePermiso('PRECIOS_MODIFICAR'),
  );

  protected readonly opcionesProveedores = computed(() => [
    { id: null, nombre: 'Sin proveedor asignado' },
    ...this.proveedores().map((p) => ({ id: p.id, nombre: p.nombre })),
  ]);

  protected readonly form = this.fb.group({
    nombre: ['', Validators.required],
    categoria: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    costo: [null as number | null, [Validators.min(0)]],
    idProveedor: [null as number | null],
  });

  constructor() {
    this.store.cargar();
    if (this.authStore.tienePermiso('PROVEEDOR_GESTIONAR', 'ORDEN_COMPRA_VER')) {
      void this.cargarProveedores();
    }
  }

  private async cargarProveedores(): Promise<void> {
    const lista = await firstValueFrom(this.comprasService.listarProveedores());
    this.proveedores.set(lista);
  }

  protected margenPorcentaje(p: Producto): number | null {
    if (p.costo == null || p.precio <= 0) return null;
    return Math.round(((p.precio - p.costo) / p.precio) * 100);
  }

  protected abrirNuevo(): void {
    this.editando.set(null);
    this.form.reset({
      nombre: '',
      categoria: '',
      precio: 0,
      costo: 0,
      idProveedor: null,
    });
    this.dialogVisible.set(true);
  }

  protected abrirEdicion(producto: Producto): void {
    this.editando.set(producto);
    this.form.reset({
      nombre: producto.nombre,
      categoria: producto.categoria ?? '',
      precio: producto.precio,
      costo: producto.costo ?? null,
      idProveedor: producto.idProveedor ?? null,
    });
    this.dialogVisible.set(true);
  }

  protected async guardar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const payload = {
      nombre: v.nombre ?? '',
      categoria: v.categoria ?? '',
      precio: v.precio ?? 0,
      costo: v.costo,
      idProveedor: v.idProveedor,
    };
    const editando = this.editando();

    if (editando) {
      await this.store.actualizar(editando.id, payload);
    } else {
      await this.store.crear(payload);
    }

    this.dialogVisible.set(false);
  }

  protected confirmarEliminar(producto: Producto): void {
    this.confirmationService.confirm({
      header: 'Eliminar producto',
      message: `¿Seguro que querés eliminar "${producto.nombre}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Eliminar' },
      rejectButtonProps: { severity: 'secondary', label: 'Cancelar', variant: 'outlined' },
      accept: () => this.store.eliminar(producto.id),
    });
  }
}
