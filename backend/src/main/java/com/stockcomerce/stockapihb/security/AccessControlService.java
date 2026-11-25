package com.stockcomerce.stockapihb.security;

import com.stockcomerce.stockapihb.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

// Motor central de autorizacion basada en riesgo/responsabilidad y alcance contextual de datos.
// Nota: todas las peticiones HTTP a /api/** pasan primero por SecurityFilterChain (autenticadas);
// solo llamadas internas del sistema sin SecurityContext (seed inicial, eventos o tests unitarios directos)
// corren como contexto de sistema.
@Service
public class AccessControlService {

    public Optional<UsuarioPrincipal> usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        if (!auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new ForbiddenException("Acceso denegado: sesion no autenticada");
        }
        if (!principal.isActivo()) {
            throw new ForbiddenException("Acceso denegado: usuario inactivo");
        }
        return Optional.of(principal);
    }

    public String usernameActual() {
        return usuarioActual().map(UsuarioPrincipal::getUsername).orElse("SISTEMA");
    }

    public boolean puedeVerCostos() {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) return true;
        return opt.get().getAsignaciones().stream()
                .anyMatch(a -> a.permisos().contains(Permiso.COSTOS_VER));
    }

    public boolean tienePermiso(Permiso... permisos) {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) return true;
        Set<Permiso> buscados = Set.of(permisos);
        return opt.get().getAsignaciones().stream()
                .anyMatch(a -> a.permisos().stream().anyMatch(buscados::contains));
    }

    public void exigirPermiso(Permiso... permisos) {
        if (!tienePermiso(permisos)) {
            throw new ForbiddenException(
                    "Permiso insuficiente: se requiere alguna de las capacidades " + Arrays.toString(permisos));
        }
    }

    public void exigirAlcanceGlobal(Permiso... permisos) {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) return;
        Set<Permiso> buscados = Set.of(permisos);
        boolean ok = opt.get().getAsignaciones().stream()
                .anyMatch(a -> a.tipoAlcance() == TipoAlcance.GLOBAL
                        && a.permisos().stream().anyMatch(buscados::contains));
        if (!ok) {
            throw new ForbiddenException(
                    "Acceso denegado: esta vista consolidada requiere alcance global con " + Arrays.toString(permisos));
        }
    }

    public void exigirPermisoEnUbicacion(Long idTienda, Permiso... permisos) {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) return;
        if (idTienda == null) {
            exigirAlcanceGlobal(permisos);
            return;
        }
        Set<Permiso> buscados = Set.of(permisos);
        boolean autorizado = opt.get().getAsignaciones().stream()
                .filter(a -> a.permisos().stream().anyMatch(buscados::contains))
                .anyMatch(a -> a.tipoAlcance() == TipoAlcance.GLOBAL
                        || idTienda.equals(a.idTienda()));
        if (!autorizado) {
            throw new ForbiddenException(
                    "Acceso denegado: no posee permiso " + Arrays.toString(permisos)
                            + " sobre la ubicacion id=" + idTienda);
        }
    }

    public AlcanceUbicacion resolverAlcance(Permiso... permisos) {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) {
            return AlcanceUbicacion.accesoGlobal();
        }
        Set<Permiso> buscados = Set.of(permisos);
        var coincidencias = opt.get().getAsignaciones().stream()
                .filter(a -> a.permisos().stream().anyMatch(buscados::contains))
                .toList();

        if (coincidencias.isEmpty()) {
            throw new ForbiddenException(
                    "Permiso insuficiente: se requiere " + Arrays.toString(permisos));
        }

        if (coincidencias.stream().anyMatch(a -> a.tipoAlcance() == TipoAlcance.GLOBAL)) {
            return AlcanceUbicacion.accesoGlobal();
        }

        Set<Long> ids = new LinkedHashSet<>();
        for (var a : coincidencias) {
            if (a.idTienda() != null) {
                ids.add(a.idTienda());
            }
        }
        return AlcanceUbicacion.restringido(ids);
    }

    // Alcance general de ubicaciones visibles del usuario (union de todas sus asignaciones activas)
    public AlcanceUbicacion resolverUbicacionesAsignadas() {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty()) {
            return AlcanceUbicacion.accesoGlobal();
        }
        var asigs = opt.get().getAsignaciones();
        if (asigs.isEmpty()) {
            throw new ForbiddenException("El usuario no tiene roles ni ubicaciones asignadas");
        }
        if (asigs.stream().anyMatch(a -> a.tipoAlcance() == TipoAlcance.GLOBAL)) {
            return AlcanceUbicacion.accesoGlobal();
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (var a : asigs) {
            if (a.idTienda() != null) {
                ids.add(a.idTienda());
            }
        }
        return AlcanceUbicacion.restringido(ids);
    }

    public Optional<String> cajaAsignadaEn(Long idTienda) {
        Optional<UsuarioPrincipal> opt = usuarioActual();
        if (opt.isEmpty() || idTienda == null) return Optional.empty();
        return opt.get().getAsignaciones().stream()
                .filter(a -> idTienda.equals(a.idTienda()) && a.permisos().contains(Permiso.POS_COBRAR))
                .map(UsuarioPrincipal.AsignacionActiva::caja)
                .filter(c -> c != null && !c.isBlank())
                .findFirst();
    }
}
