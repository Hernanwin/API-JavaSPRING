package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaDTO {
    private Long id;
    private Long idProducto;
    private String nombreProducto;
    private Long idTiendaOrigen;
    private String nombreTiendaOrigen;
    private Long idTiendaDestino;
    private String nombreTiendaDestino;
    private Integer cantidad;
    private LocalDateTime fecha;
}
