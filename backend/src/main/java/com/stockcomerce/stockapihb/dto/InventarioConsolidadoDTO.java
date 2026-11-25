package com.stockcomerce.stockapihb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventarioConsolidadoDTO {
    private Long idProducto;
    private String nombreProducto;
    private String categoria;
    private Long idProveedor;
    private String nombreProveedor;
    private Integer totalUnidades;
    private Integer totalReservadas;
    private Integer totalDisponibles;
    private Integer ubicacionesConStock;
    private BigDecimal precioVenta;
    private BigDecimal costoUnitario;
    private BigDecimal valorTotalCosto;
}
