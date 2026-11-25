package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.security.Permiso;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private boolean activo;
    private List<AsignacionRolDTO> asignaciones;
    private Set<Permiso> permisosEfectivos;
}
