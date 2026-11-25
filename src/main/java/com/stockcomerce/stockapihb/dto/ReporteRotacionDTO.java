package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteRotacionDTO {
    private Long idProducto;
    private String nombreProducto;
    private Long idProveedor;
    private String nombreProveedor;
    private Long idTienda;
    private String nombreTienda;
    private Integer stockActual;
    private Integer unidadesSalidasPeriodo;
    private BigDecimal indiceRotacion;
    private BigDecimal diasInventarioEstimados;
}
