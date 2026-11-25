package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.models.TipoUbicacion;
import com.stockcomerce.stockapihb.security.NivelRiesgo;
import com.stockcomerce.stockapihb.security.Permiso;
import com.stockcomerce.stockapihb.security.RolCodigo;
import com.stockcomerce.stockapihb.security.TipoAlcance;
import lombok.*;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDTO {
    private Long id;
    private RolCodigo codigo;
    private String nombre;
    private String descripcion;
    private NivelRiesgo nivelRiesgo;
    private TipoAlcance tipoAlcance;
    private TipoUbicacion tipoUbicacionRequerido;
    private Set<Permiso> permisos;
}
