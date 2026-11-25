package com.stockcomerce.stockapihb.security;

import com.stockcomerce.stockapihb.models.AsignacionRol;
import com.stockcomerce.stockapihb.models.TipoUbicacion;
import com.stockcomerce.stockapihb.models.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class UsuarioPrincipal implements UserDetails {

    public record AsignacionActiva(
            RolCodigo rolCodigo,
            NivelRiesgo nivelRiesgo,
            TipoAlcance tipoAlcance,
            Set<Permiso> permisos,
            Long idTienda,
            String nombreTienda,
            TipoUbicacion tipoUbicacion,
            String caja
    ) {}

    private final Long id;
    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final boolean activo;
    private final List<AsignacionActiva> asignaciones;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(Long id,
                            String username,
                            String password,
                            String nombreCompleto,
                            boolean activo,
                            List<AsignacionActiva> asignaciones) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.activo = activo;
        this.asignaciones = List.copyOf(asignaciones);

        Set<GrantedAuthority> auths = new LinkedHashSet<>();
        for (AsignacionActiva a : asignaciones) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + a.rolCodigo().name()));
            for (Permiso p : a.permisos()) {
                auths.add(new SimpleGrantedAuthority(p.name()));
            }
        }
        this.authorities = Collections.unmodifiableSet(auths);
    }

    public static UsuarioPrincipal fromEntity(Usuario u) {
        List<AsignacionActiva> activas = new ArrayList<>();
        if (u.getAsignaciones() != null) {
            for (AsignacionRol ar : u.getAsignaciones()) {
                if (!ar.isActivo() || ar.getRol() == null) continue;
                activas.add(new AsignacionActiva(
                        ar.getRol().getCodigo(),
                        ar.getRol().getNivelRiesgo(),
                        ar.getRol().getTipoAlcance(),
                        Set.copyOf(ar.getRol().getPermisos()),
                        ar.getTienda() != null ? ar.getTienda().getId() : null,
                        ar.getTienda() != null ? ar.getTienda().getNombre() : null,
                        ar.getTienda() != null ? ar.getTienda().getTipo() : null,
                        ar.getCaja()
                ));
            }
        }
        return new UsuarioPrincipal(
                u.getId(),
                u.getUsername(),
                u.getPasswordHash(),
                u.getNombreCompleto(),
                u.isActivo(),
                activas
        );
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
