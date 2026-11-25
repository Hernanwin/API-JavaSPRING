package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {
    private Long id;
    private String tipo;
    private Long idProducto;
    private String nombreProducto;
    private Long idTienda;
    private String nombreTienda;
    private Integer cantidadActual;
    private Integer stockMinimo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
}
