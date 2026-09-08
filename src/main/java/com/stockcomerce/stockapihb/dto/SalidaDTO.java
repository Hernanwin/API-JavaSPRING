package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalidaDTO {
    private Long id;
    private Long idProducto;
    private String nombreProducto;
    private Long idTienda;
    private String nombreTienda;
    private Long idVenta;
    private Integer cantidad;
    private LocalDateTime fecha;
}
