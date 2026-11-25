package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteFinancieroConsolidadoDTO {
    private Integer cantidadVentas;
    private BigDecimal ingresosTotalesVentas;
    private BigDecimal costoTotalVentas;
    private BigDecimal utilidadBrutaVentas;
    private BigDecimal margenBrutoPorcentaje;
    private BigDecimal valorInventarioAlCosto;
    private BigDecimal valorInventarioAPrecioVenta;
    private BigDecimal apartadosActivosTotal;
    private BigDecimal apartadosSaldoPendiente;
    private BigDecimal ordenesCompraComprometidas;
}
