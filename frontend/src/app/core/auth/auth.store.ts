import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import {
  AsignacionRol,
  ETIQUETA_ROL,
  LoginRequest,
  Permiso,
  ROL_PERMISO_MAP,
  UsuarioPerfil,
} from './auth.model';
import { TiendaSeleccionadaStore } from '../state/tienda-seleccionada.store';

const STORAGE_TOKEN_KEY = 'stockapihb_jwt_token';
const STORAGE_USER_KEY = 'stockapihb_user_profile';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly tiendaSeleccionada = inject(TiendaSeleccionadaStore);

  private readonly _token = signal<string | null>(this.leerTokenValido());
  private readonly _usuario = signal<UsuarioPerfil | null>(this.leerUsuarioGuardado());
  private readonly _cargando = signal(false);

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly cargando = this._cargando.asReadonly();

  readonly autenticado = computed(() => Boolean(this._token() && this._usuario()));

  readonly asignacionesActivas = computed<AsignacionRol[]>(() =>
    (this._usuario()?.asignaciones ?? []).filter((a) => a.activo !== false),
  );

  readonly permisosEfectivos = computed<Set<Permiso>>(
    () => new Set(this._usuario()?.permisosEfectivos ?? []),
  );

  readonly puedeVerCostos = computed(() => this.permisosEfectivos().has('COSTOS_VER'));

  readonly esAlcanceGlobal = computed(() =>
    this.asignacionesActivas().some((a) => a.tipoAlcance === 'GLOBAL'),
  );

  readonly resumenRoles = computed(() => {
    const vistos = new Set<string>();
    const lista: string[] = [];
    for (const a of this.asignacionesActivas()) {
      const nombre = ETIQUETA_ROL[a.rol] ?? a.rol;
      if (!vistos.has(nombre)) {
        vistos.add(nombre);
        lista.push(nombre);
      }
    }
    return lista.join(' · ') || 'Sin rol asignado';
  });

  constructor() {
    if (this._token()) {
      void this.refrescarPerfilSilencioso();
    }
  }

  async login(credenciales: LoginRequest): Promise<void> {
    this._cargando.set(true);
    try {
      const resp = await firstValueFrom(this.authService.login(credenciales));
      localStorage.setItem(STORAGE_TOKEN_KEY, resp.token);
      localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(resp.usuario));
      this._token.set(resp.token);
      this._usuario.set(resp.usuario);
      this.sincronizarSeleccionInicial(resp.usuario);
    } finally {
      this._cargando.set(false);
    }
  }

  logout(redirigir = true): void {
    localStorage.removeItem(STORAGE_TOKEN_KEY);
    localStorage.removeItem(STORAGE_USER_KEY);
    this._token.set(null);
    this._usuario.set(null);
    this.tiendaSeleccionada.seleccionar(null);
    if (redirigir) {
      void this.router.navigate(['/login']);
    }
  }

  tienePermiso(...permisos: Permiso[]): boolean {
    const set = this.permisosEfectivos();
    return permisos.some((p) => set.has(p));
  }

  tieneAlcanceGlobal(...permisos: Permiso[]): boolean {
    return this.asignacionesActivas().some((a) => {
      if (a.tipoAlcance !== 'GLOBAL') return false;
      const permsRol = ROL_PERMISO_MAP[a.rol] ?? [];
      return permisos.some((p) => permsRol.includes(p));
    });
  }

  tienePermisoEnUbicacion(idTienda: number | null | undefined, ...permisos: Permiso[]): boolean {
    if (idTienda == null) {
      return this.tieneAlcanceGlobal(...permisos);
    }
    return this.asignacionesActivas().some((a) => {
      const permsRol = ROL_PERMISO_MAP[a.rol] ?? [];
      if (!permisos.some((p) => permsRol.includes(p))) return false;
      return a.tipoAlcance === 'GLOBAL' || a.idTienda === idTienda;
    });
  }

  // null = alcance GLOBAL (todas las ubicaciones); number[] = solo esos IDs
  idsUbicacionesConPermiso(...permisos: Permiso[]): number[] | null {
    const coincidencias = this.asignacionesActivas().filter((a) => {
      const permsRol = ROL_PERMISO_MAP[a.rol] ?? [];
      return permisos.some((p) => permsRol.includes(p));
    });
    if (coincidencias.some((a) => a.tipoAlcance === 'GLOBAL')) {
      return null;
    }
    const ids = new Set<number>();
    for (const c of coincidencias) {
      if (c.idTienda != null) ids.add(c.idTienda);
    }
    return [...ids];
  }

  cajaAsignadaEn(idTienda: number | null | undefined): string | null {
    if (idTienda == null) return null;
    const match = this.asignacionesActivas().find(
      (a) => a.idTienda === idTienda && a.rol === 'CAJERO_VENDEDOR' && a.caja,
    );
    return match?.caja ?? null;
  }

  rutaPorDefecto(): string {
    if (this.tienePermiso('POS_COBRAR') && !this.tienePermiso('REPORTE_OPERATIVO_VER')) {
      return '/pos';
    }
    return '/resumen';
  }

  private async refrescarPerfilSilencioso(): Promise<void> {
    try {
      const perfil = await firstValueFrom(this.authService.perfilActual());
      localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(perfil));
      this._usuario.set(perfil);
      this.sincronizarSeleccionInicial(perfil);
    } catch {
      this.logout(false);
    }
  }

  private sincronizarSeleccionInicial(perfil: UsuarioPerfil): void {
    const activas = (perfil.asignaciones ?? []).filter((a) => a.activo !== false);
    const esGlobal = activas.some((a) => a.tipoAlcance === 'GLOBAL');
    if (esGlobal) {
      this.tiendaSeleccionada.seleccionar(null);
      return;
    }
    const ids = [...new Set(activas.map((a) => a.idTienda).filter((id): id is number => id != null))];
    // Si el usuario tiene acceso a una sola sucursal/deposito, la preselecciona automaticamente
    this.tiendaSeleccionada.seleccionar(ids.length === 1 ? ids[0] : null);
  }

  private leerTokenValido(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const raw = localStorage.getItem(STORAGE_TOKEN_KEY);
    if (!raw) return null;
    try {
      const payload = JSON.parse(atob(raw.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      if (typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now()) {
        localStorage.removeItem(STORAGE_TOKEN_KEY);
        localStorage.removeItem(STORAGE_USER_KEY);
        return null;
      }
      return raw;
    } catch {
      return null;
    }
  }

  private leerUsuarioGuardado(): UsuarioPerfil | null {
    if (typeof localStorage === 'undefined' || !this._token()) return null;
    const raw = localStorage.getItem(STORAGE_USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UsuarioPerfil;
    } catch {
      return null;
    }
  }
}
