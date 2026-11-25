import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';

import { UsuariosStore } from '../../data/usuarios.store';
import {
  AsignacionRol,
  ETIQUETA_ROL,
  NivelRiesgo,
  RolCodigo,
  UsuarioPerfil,
} from '../../../../core/auth/auth.model';

@Component({
  selector: 'app-usuarios-page',
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    SelectModule,
    InputTextModule,
    PasswordModule,
    TagModule,
  ],
  templateUrl: './usuarios-page.html',
})
export class UsuariosPage {
  protected readonly store = inject(UsuariosStore);
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);

  protected readonly tabActiva = signal<'usuarios' | 'roles'>('usuarios');
  protected readonly dialogCrearVisible = signal(false);
  protected readonly dialogAsignacionesVisible = signal(false);
  protected readonly usuarioEditando = signal<UsuarioPerfil | null>(null);

  protected nombreRol(rol: string): string {
    return (ETIQUETA_ROL as Record<string, string>)[rol] ?? rol;
  }

  protected readonly opcionesRol: { label: string; value: RolCodigo }[] = [
    { label: 'Encargado de Depósito (Alcance: DEPÓSITO)', value: 'ENCARGADO_DEPOSITO' },
    { label: 'Cajero / Vendedor (Alcance: TIENDA / POS)', value: 'CAJERO_VENDEDOR' },
    { label: 'Supervisor de Compras (Alcance: GLOBAL)', value: 'SUPERVISOR_COMPRAS' },
    { label: 'Administrador General (Alcance: GLOBAL)', value: 'ADMIN' },
  ];

  protected readonly formCrear = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
    nombreCompleto: ['', Validators.required],
    asignaciones: this.fb.array([this.crearGrupoAsignacion()]),
  });

  protected readonly formAsignaciones = this.fb.group({
    asignaciones: this.fb.array([]),
  });

  constructor() {
    void this.store.cargar();
  }

  protected get asignacionesCrear(): FormArray {
    return this.formCrear.get('asignaciones') as FormArray;
  }

  protected get asignacionesEditar(): FormArray {
    return this.formAsignaciones.get('asignaciones') as FormArray;
  }

  private crearGrupoAsignacion(inicial?: Partial<AsignacionRol>) {
    const rol = inicial?.rol ?? 'CAJERO_VENDEDOR';
    const ubicaciones = this.store.ubicacionesAdmitidasParaRol(rol);
    const idTienda =
      inicial?.idTienda !== undefined
        ? inicial.idTienda
        : this.store.esRolDeUbicacion(rol)
          ? (ubicaciones[0]?.id ?? null)
          : null;

    return this.fb.group({
      rol: [rol as RolCodigo, Validators.required],
      idTienda: [idTienda as number | null],
      caja: [inicial?.caja ?? ''],
    });
  }

  protected alCambiarRolFila(arr: FormArray, idx: number): void {
    const grupo = arr.at(idx);
    const rol = grupo.get('rol')?.value as RolCodigo;
    if (!this.store.esRolDeUbicacion(rol)) {
      grupo.patchValue({ idTienda: null, caja: '' });
    } else {
      const ubs = this.store.ubicacionesAdmitidasParaRol(rol);
      grupo.patchValue({
        idTienda: ubs[0]?.id ?? null,
        caja: rol === 'CAJERO_VENDEDOR' ? 'CAJA-01' : '',
      });
    }
  }

  protected agregarAsignacion(arr: FormArray): void {
    arr.push(this.crearGrupoAsignacion());
  }

  protected quitarAsignacion(arr: FormArray, idx: number): void {
    if (arr.length > 1) arr.removeAt(idx);
  }

  protected abrirNuevoUsuario(): void {
    this.asignacionesCrear.clear();
    this.asignacionesCrear.push(this.crearGrupoAsignacion());
    this.formCrear.patchValue({
      username: '',
      password: '',
      nombreCompleto: '',
    });
    this.dialogCrearVisible.set(true);
  }

  protected abrirEditarAsignaciones(u: UsuarioPerfil): void {
    this.usuarioEditando.set(u);
    this.asignacionesEditar.clear();
    for (const a of u.asignaciones) {
      this.asignacionesEditar.push(this.crearGrupoAsignacion(a));
    }
    if (this.asignacionesEditar.length === 0) {
      this.asignacionesEditar.push(this.crearGrupoAsignacion());
    }
    this.dialogAsignacionesVisible.set(true);
  }

  protected async guardarNuevoUsuario(): Promise<void> {
    if (this.formCrear.invalid) {
      this.formCrear.markAllAsTouched();
      return;
    }
    const raw = this.formCrear.getRawValue();
    await this.store.crearUsuario({
      username: raw.username!,
      password: raw.password!,
      nombreCompleto: raw.nombreCompleto!,
      asignaciones: this.normalizarAsignaciones(raw.asignaciones),
    });
    this.messageService.add({
      severity: 'success',
      summary: 'Usuario creado',
      detail: `Se registró @${raw.username} con sus roles y alcance asignado.`,
    });
    this.dialogCrearVisible.set(false);
  }

  protected async guardarAsignaciones(): Promise<void> {
    const u = this.usuarioEditando();
    if (!u) return;
    const raw = this.formAsignaciones.getRawValue();
    await this.store.actualizarAsignaciones(
      u.id,
      this.normalizarAsignaciones(raw.asignaciones as { rol: RolCodigo; idTienda: number | null; caja: string | null }[]),
    );
    this.messageService.add({
      severity: 'success',
      summary: 'Alcance actualizado',
      detail: `Se actualizaron las asignaciones de @${u.username}.`,
    });
    this.dialogAsignacionesVisible.set(false);
  }

  protected async alternarActivo(u: UsuarioPerfil): Promise<void> {
    await this.store.cambiarEstado(u.id, !u.activo);
    this.messageService.add({
      severity: 'info',
      summary: u.activo ? 'Cuenta suspendida' : 'Cuenta reactivada',
      detail: `Usuario @${u.username} ${u.activo ? 'deshabilitado' : 'activo'}.`,
    });
  }

  private normalizarAsignaciones(
    filas: { rol: RolCodigo | null; idTienda: number | null; caja: string | null }[],
  ): AsignacionRol[] {
    return filas.map((f) => {
      const rol = f.rol!;
      const requiereUbic = this.store.esRolDeUbicacion(rol);
      return {
        rol,
        idTienda: requiereUbic ? f.idTienda : null,
        caja: rol === 'CAJERO_VENDEDOR' && f.caja ? f.caja : undefined,
        activo: true,
      };
    });
  }

  protected severidadRiesgo(nivel?: NivelRiesgo): 'danger' | 'warn' | 'info' | 'success' {
    switch (nivel) {
      case 'CRITICO':
        return 'danger';
      case 'ALTO':
        return 'warn';
      case 'MEDIO':
        return 'info';
      default:
        return 'success';
    }
  }
}
