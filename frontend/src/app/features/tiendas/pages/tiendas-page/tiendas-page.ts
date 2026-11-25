import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ConfirmationService } from 'primeng/api';

import { TiendaStore } from '../../data/tienda.store';
import { Tienda } from '../../data/tienda.model';
import { AuthStore } from '../../../../core/auth/auth.store';
import { TipoUbicacion } from '../../../../core/auth/auth.model';

@Component({
  selector: 'app-tiendas-page',
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    TagModule,
  ],
  templateUrl: './tiendas-page.html',
})
export class TiendasPage {
  protected readonly store = inject(TiendaStore);
  protected readonly authStore = inject(AuthStore);
  private readonly fb = inject(FormBuilder);
  private readonly confirmationService = inject(ConfirmationService);

  protected readonly puedeConfigurar = computed(() =>
    this.authStore.tienePermiso('TIENDA_CONFIGURAR'),
  );

  protected readonly dialogVisible = signal(false);
  protected readonly editando = signal<Tienda | null>(null);

  protected readonly opcionesTipo: { label: string; value: TipoUbicacion }[] = [
    { label: 'TIENDA (Sucursal / POS)', value: 'TIENDA' },
    { label: 'DEPÓSITO (Almacén logístico)', value: 'DEPOSITO' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    direccion: [''],
    tipo: ['TIENDA' as TipoUbicacion, Validators.required],
  });

  constructor() {
    this.store.cargar();
  }

  protected abrirNuevo(): void {
    this.editando.set(null);
    this.form.reset({ nombre: '', direccion: '', tipo: 'TIENDA' });
    this.dialogVisible.set(true);
  }

  protected abrirEdicion(tienda: Tienda): void {
    this.editando.set(tienda);
    this.form.reset({
      nombre: tienda.nombre,
      direccion: tienda.direccion,
      tipo: tienda.tipo ?? 'TIENDA',
    });
    this.dialogVisible.set(true);
  }

  protected async guardar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const editando = this.editando();

    if (editando) {
      await this.store.actualizar(editando.id, valores);
    } else {
      await this.store.crear(valores);
    }

    this.dialogVisible.set(false);
  }

  protected confirmarEliminar(tienda: Tienda): void {
    this.confirmationService.confirm({
      header: 'Eliminar ubicación',
      message: `¿Seguro que querés eliminar "${tienda.nombre}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptButtonProps: { severity: 'danger', label: 'Eliminar' },
      rejectButtonProps: { severity: 'secondary', label: 'Cancelar', variant: 'outlined' },
      accept: () => this.store.eliminar(tienda.id),
    });
  }
}
