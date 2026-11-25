package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteMargenProductoDTO {
    private Long idProducto;
    private String nombreProducto;
    private Long idProveedor;
    private String nombreProveedor;
    private BigDecimal precioVentaActual;
    private BigDecimal costoActual;
    private BigDecimal margenUnitarioCatalogo;
    private BigDecimal porcentajeMargenCatalogo;
    private Integer unidadesVendidas;
    private BigDecimal ingresosVentas;
    private BigDecimal costoVentas;
    private BigDecimal utilidadBruta;
    private BigDecimal porcentajeMargenRealizado;
}
