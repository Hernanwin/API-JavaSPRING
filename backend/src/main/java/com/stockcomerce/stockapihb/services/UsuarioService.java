package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.*;
import com.stockcomerce.stockapihb.exception.ConflictException;
import com.stockcomerce.stockapihb.exception.ForbiddenException;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.models.AsignacionRol;
import com.stockcomerce.stockapihb.models.Rol;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.TipoUbicacion;
import com.stockcomerce.stockapihb.models.Usuario;
import com.stockcomerce.stockapihb.repositories.RolRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.repositories.UsuarioRepository;
import com.stockcomerce.stockapihb.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private RolRepository rolRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        Usuario entidad = usuarioRepo.findConAsignacionesByUsername(principal.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String token = jwtService.generarToken(entidad.getId(), entidad.getUsername());
        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiracionSegundos(jwtService.getExpirationSeconds())
                .usuario(toUsuarioDTO(entidad))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO perfilActual() {
        UsuarioPrincipal principal = accessControl.usuarioActual()
                .orElseThrow(() -> new ForbiddenException("No hay un usuario autenticado en la sesion"));
        Usuario entidad = usuarioRepo.findConAsignacionesByUsername(principal.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return toUsuarioDTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarRoles() {
        accessControl.exigirPermiso(Permiso.USUARIO_GESTIONAR);
        return rolRepo.findAll().stream()
                .sorted(Comparator.comparing(r -> r.getCodigo().name()))
                .map(this::toRolDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        accessControl.exigirPermiso(Permiso.USUARIO_GESTIONAR);
        return usuarioRepo.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getUsername))
                .map(this::toUsuarioDTO)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(CrearUsuarioDTO dto) {
        accessControl.exigirPermiso(Permiso.USUARIO_GESTIONAR);
        if (dto == null) throw new IllegalArgumentException("Datos de usuario obligatorios");
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new IllegalArgumentException("El username es obligatorio");
        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new IllegalArgumentException("La contrasena es obligatoria");
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().isBlank())
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        if (dto.getAsignaciones() == null || dto.getAsignaciones().isEmpty())
            throw new IllegalArgumentException("Debe indicar al menos una asignacion de rol");

        String limpio = dto.getUsername().trim();
        if (usuarioRepo.existsByUsername(limpio)) {
            throw new ConflictException("Ya existe un usuario con username '" + limpio + "'");
        }

        Usuario usuario = Usuario.builder()
                .username(limpio)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .nombreCompleto(dto.getNombreCompleto().trim())
                .activo(true)
                .asignaciones(new ArrayList<>())
                .build();

        aplicarAsignaciones(usuario, dto.getAsignaciones());
        return toUsuarioDTO(usuarioRepo.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarAsignaciones(Long idUsuario, List<AsignacionRolDTO> asignaciones) {
        accessControl.exigirPermiso(Permiso.USUARIO_GESTIONAR);
        if (asignaciones == null || asignaciones.isEmpty()) {
            throw new IllegalArgumentException("El usuario debe conservar al menos una asignacion de rol");
        }
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.getAsignaciones().clear();
        aplicarAsignaciones(usuario, asignaciones);
        return toUsuarioDTO(usuarioRepo.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO cambiarEstado(Long idUsuario, boolean activo) {
        accessControl.exigirPermiso(Permiso.USUARIO_GESTIONAR);
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return toUsuarioDTO(usuarioRepo.save(usuario));
    }

    private void aplicarAsignaciones(Usuario usuario, List<AsignacionRolDTO> dtos) {
        for (AsignacionRolDTO item : dtos) {
            if (item.getRol() == null) {
                throw new IllegalArgumentException("Toda asignacion debe indicar el rol");
            }
            Rol rol = rolRepo.findByCodigo(item.getRol())
                    .orElseThrow(() -> new NotFoundException("Rol no registrado: " + item.getRol()));

            Tienda tienda = null;
            if (rol.getTipoAlcance() == TipoAlcance.UBICACION) {
                if (item.getIdTienda() == null) {
                    throw new IllegalArgumentException(
                            "El rol " + rol.getCodigo() + " requiere indicar idTienda (alcance por ubicacion)");
                }
                tienda = tiendaRepo.findById(item.getIdTienda())
                        .orElseThrow(() -> new NotFoundException("Ubicacion no encontrada: " + item.getIdTienda()));

                TipoUbicacion requerido = rol.getTipoUbicacionRequerido();
                TipoUbicacion actual = tienda.getTipo() != null ? tienda.getTipo() : TipoUbicacion.TIENDA;
                if (requerido != null && actual != requerido) {
                    throw new IllegalArgumentException(
                            "El rol " + rol.getCodigo() + " solo puede asignarse a ubicaciones de tipo "
                                    + requerido + " (la ubicacion '" + tienda.getNombre() + "' es " + actual + ")");
                }
            } else if (item.getIdTienda() != null) {
                throw new IllegalArgumentException(
                        "El rol " + rol.getCodigo() + " es de alcance GLOBAL y no admite idTienda especifico");
            }

            AsignacionRol entidad = AsignacionRol.builder()
                    .usuario(usuario)
                    .rol(rol)
                    .tienda(tienda)
                    .caja(item.getCaja())
                    .activo(item.getActivo() == null || item.getActivo())
                    .build();
            usuario.getAsignaciones().add(entidad);
        }
    }

    private RolDTO toRolDTO(Rol r) {
        return RolDTO.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .nivelRiesgo(r.getNivelRiesgo())
                .tipoAlcance(r.getTipoAlcance())
                .tipoUbicacionRequerido(r.getTipoUbicacionRequerido())
                .permisos(new TreeSet<>(r.getPermisos()))
                .build();
    }

    private UsuarioDTO toUsuarioDTO(Usuario u) {
        Set<Permiso> efectivos = new TreeSet<>();
        List<AsignacionRolDTO> asigs = new ArrayList<>();
        if (u.getAsignaciones() != null) {
            for (AsignacionRol ar : u.getAsignaciones()) {
                if (ar.isActivo() && ar.getRol() != null) {
                    efectivos.addAll(ar.getRol().getPermisos());
                }
                asigs.add(AsignacionRolDTO.builder()
                        .id(ar.getId())
                        .rol(ar.getRol() != null ? ar.getRol().getCodigo() : null)
                        .nivelRiesgo(ar.getRol() != null ? ar.getRol().getNivelRiesgo() : null)
                        .tipoAlcance(ar.getRol() != null ? ar.getRol().getTipoAlcance() : null)
                        .idTienda(ar.getTienda() != null ? ar.getTienda().getId() : null)
                        .nombreTienda(ar.getTienda() != null ? ar.getTienda().getNombre() : null)
                        .tipoUbicacion(ar.getTienda() != null ? ar.getTienda().getTipo() : null)
                        .caja(ar.getCaja())
                        .activo(ar.isActivo())
                        .build());
            }
        }
        return UsuarioDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .activo(u.isActivo())
                .asignaciones(asigs)
                .permisosEfectivos(efectivos)
                .build();
    }
}
