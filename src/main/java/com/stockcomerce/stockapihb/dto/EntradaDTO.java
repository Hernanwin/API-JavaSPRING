package com.stockcomerce.stockapihb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaDTO {
    private Long id;
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto;
    private String nombreProducto;
    @NotNull(message = "Debe indicar la tienda")
    private Long idTienda;
    private String nombreTienda;
    @NotNull(message = "Debe indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    private LocalDateTime fecha;
    private String observacion;
}
