package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteCumplimientoRecepcionDTO {
    private Long idTienda;
    private String nombreTienda;
    private Integer ordenesCompraTotales;
    private Integer ordenesCompraCompletas;
    private Integer ordenesCompraParciales;
    private Integer ordenesCompraPendientes;
    private Integer unidadesOcSolicitadas;
    private Integer unidadesOcRecibidas;
    private BigDecimal porcentajeCumplimientoOc;
    private Integer transferenciasCompletadas;
    private Integer transferenciasEnTransito;
    private Integer unidadesTransfEnviadas;
    private Integer unidadesTransfRecibidas;
    private BigDecimal porcentajeCumplimientoTransferencias;
}
