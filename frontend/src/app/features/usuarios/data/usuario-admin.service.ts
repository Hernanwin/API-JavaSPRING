import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import {
  AsignacionRol,
  CrearUsuarioInput,
  RolDetalle,
  UsuarioPerfil,
} from '../../../core/auth/auth.model';

@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private readonly http = inject(HttpClient);

  listarRoles(): Observable<RolDetalle[]> {
    return this.http.get<RolDetalle[]>(`${API_BASE_URL}/roles`);
  }

  listarUsuarios(): Observable<UsuarioPerfil[]> {
    return this.http.get<UsuarioPerfil[]>(`${API_BASE_URL}/usuarios`);
  }

  crearUsuario(input: CrearUsuarioInput): Observable<UsuarioPerfil> {
    return this.http.post<UsuarioPerfil>(`${API_BASE_URL}/usuarios`, input);
  }

  actualizarAsignaciones(
    idUsuario: number,
    asignaciones: AsignacionRol[],
  ): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>(
      `${API_BASE_URL}/usuarios/${idUsuario}/asignaciones`,
      asignaciones,
    );
  }

  cambiarEstado(idUsuario: number, activo: boolean): Observable<UsuarioPerfil> {
    const params = new HttpParams().set('activo', activo);
    return this.http.put<UsuarioPerfil>(`${API_BASE_URL}/usuarios/${idUsuario}/estado`, null, {
      params,
    });
  }
}
