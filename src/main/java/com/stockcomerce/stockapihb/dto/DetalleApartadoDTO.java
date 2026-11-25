package com.stockcomerce.stockapihb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleApartadoDTO {
    private Long id;
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto;
    private String nombreProducto;
    @NotNull(message = "Debe indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
