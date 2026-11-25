import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { UsuarioAdminService } from './usuario-admin.service';
import {
  AsignacionRol,
  CrearUsuarioInput,
  RolCodigo,
  RolDetalle,
  UsuarioPerfil,
} from '../../../core/auth/auth.model';
import { TiendaService } from '../../tiendas/data/tienda.service';
import { Tienda } from '../../tiendas/data/tienda.model';

@Injectable({ providedIn: 'root' })
export class UsuariosStore {
  private readonly adminService = inject(UsuarioAdminService);
  private readonly tiendaService = inject(TiendaService);

  private readonly _usuarios = signal<UsuarioPerfil[]>([]);
  private readonly _roles = signal<RolDetalle[]>([]);
  private readonly _tiendas = signal<Tienda[]>([]);
  private readonly _cargando = signal(false);

  readonly usuarios = this._usuarios.asReadonly();
  readonly roles = this._roles.asReadonly();
  readonly tiendas = this._tiendas.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  readonly depositos = computed(() => this._tiendas().filter((t) => t.tipo === 'DEPOSITO'));
  readonly sucursales = computed(() => this._tiendas().filter((t) => t.tipo !== 'DEPOSITO'));

  async cargar(): Promise<void> {
    this._cargando.set(true);
    try {
      const [usuarios, roles, tiendas] = await Promise.all([
        firstValueFrom(this.adminService.listarUsuarios()),
        firstValueFrom(this.adminService.listarRoles()),
        firstValueFrom(this.tiendaService.listar()),
      ]);
      this._usuarios.set(usuarios);
      this._roles.set(roles);
      this._tiendas.set(tiendas);
    } finally {
      this._cargando.set(false);
    }
  }

  ubicacionesAdmitidasParaRol(rol: RolCodigo | null): Tienda[] {
    if (rol === 'ENCARGADO_DEPOSITO') return this.depositos();
    if (rol === 'CAJERO_VENDEDOR') return this.sucursales();
    return [];
  }

  esRolDeUbicacion(rol: RolCodigo | null): boolean {
    return rol === 'ENCARGADO_DEPOSITO' || rol === 'CAJERO_VENDEDOR';
  }

  async crearUsuario(input: CrearUsuarioInput): Promise<void> {
    const creado = await firstValueFrom(this.adminService.crearUsuario(input));
    this._usuarios.update((lista) => [...lista, creado]);
  }

  async actualizarAsignaciones(idUsuario: number, asignaciones: AsignacionRol[]): Promise<void> {
    const actualizado = await firstValueFrom(
      this.adminService.actualizarAsignaciones(idUsuario, asignaciones),
    );
    this._usuarios.update((lista) =>
      lista.map((u) => (u.id === idUsuario ? actualizado : u)),
    );
  }

  async cambiarEstado(idUsuario: number, activo: boolean): Promise<void> {
    const actualizado = await firstValueFrom(
      this.adminService.cambiarEstado(idUsuario, activo),
    );
    this._usuarios.update((lista) =>
      lista.map((u) => (u.id === idUsuario ? actualizado : u)),
    );
  }
}
