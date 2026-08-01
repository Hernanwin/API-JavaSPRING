package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class VentaDTO {
    //datos de la venta
    private Long id;
    private LocalDateTime fechaHora;
    private String estado;

    //datos de la tienda
    private Long idTienda;

    //listado de detalles
    private List<DetalleVentaDTO> detalle;

    //total de la venta en si
    private Double total;
}