package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.*;

import java.util.List;

public interface IUsuarioService {
    LoginResponseDTO login(LoginRequestDTO request);
    UsuarioDTO perfilActual();
    List<RolDTO> listarRoles();
    List<UsuarioDTO> listarUsuarios();
    UsuarioDTO crearUsuario(CrearUsuarioDTO dto);
    UsuarioDTO actualizarAsignaciones(Long idUsuario, List<AsignacionRolDTO> asignaciones);
    UsuarioDTO cambiarEstado(Long idUsuario, boolean activo);
}
