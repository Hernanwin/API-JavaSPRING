package com.stockcomerce.stockapihb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteConteoDTO {
    private Long id;
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto;
    private String nombreProducto;
    @NotNull(message = "Debe indicar el deposito/ubicacion")
    private Long idTienda;
    private String nombreTienda;
    private Integer cantidadSistemaAnterior;
    @NotNull(message = "Debe indicar la cantidad fisica contada")
    @PositiveOrZero(message = "La cantidad contada no puede ser negativa")
    private Integer cantidadContada;
    private Integer diferencia;
    private String motivo;
    private String ejecutadoPor;
    private LocalDateTime fecha;
}
