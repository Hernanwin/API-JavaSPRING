package com.stockcomerce.stockapihb.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenStockTiendaDTO {
    private Long idTienda;
    private String nombreTienda;
    private Integer totalUnidades;
    private Integer productosDistintos;
}
