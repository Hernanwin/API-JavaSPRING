package com.stockcomerce.stockapihb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoDTO {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private BigDecimal precio;
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private BigDecimal costo;
    private String categoria;
    private Long idProveedor;
    private String nombreProveedor;
    // el stock ahora se consulta por /api/inventario (Producto + Tienda), no por aca
}
