package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.models.TipoUbicacion;
import com.stockcomerce.stockapihb.security.NivelRiesgo;
import com.stockcomerce.stockapihb.security.RolCodigo;
import com.stockcomerce.stockapihb.security.TipoAlcance;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionRolDTO {
    private Long id;
    @NotNull(message = "Debe indicar el codigo de rol")
    private RolCodigo rol;
    private NivelRiesgo nivelRiesgo;
    private TipoAlcance tipoAlcance;
    // Obligatorio para roles con tipoAlcance = UBICACION (ENCARGADO_DEPOSITO, CAJERO_VENDEDOR)
    private Long idTienda;
    private String nombreTienda;
    private TipoUbicacion tipoUbicacion;
    private String caja;
    @Builder.Default
    private Boolean activo = true;
}
