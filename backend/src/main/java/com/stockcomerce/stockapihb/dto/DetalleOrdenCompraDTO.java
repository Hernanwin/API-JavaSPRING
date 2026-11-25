package com.stockcomerce.stockapihb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetalleOrdenCompraDTO {
    private Long id;
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto;
    private String nombreProducto;
    @NotNull(message = "Debe indicar la cantidad solicitada")
    @Positive(message = "La cantidad solicitada debe ser mayor a 0")
    private Integer cantidadSolicitada;
    private Integer cantidadRecibida;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
}
