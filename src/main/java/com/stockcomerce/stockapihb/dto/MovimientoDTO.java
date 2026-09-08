package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.time.LocalDateTime;

// vista de solo lectura para reportes: junta Entrada, Salida y Transferencia
// en una sola linea de tiempo. No es una tabla propia.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoDTO {
    private String tipo; // ENTRADA, SALIDA o TRANSFERENCIA

    private Long idProducto;
    private String nombreProducto;

    // tienda afectada: unica en Entrada/Salida, origen en Transferencia
    private Long idTienda;
    private String nombreTienda;

    // solo se completa cuando tipo = TRANSFERENCIA
    private Long idTiendaDestino;
    private String nombreTiendaDestino;

    private Integer cantidad;
    private LocalDateTime fecha;
}
