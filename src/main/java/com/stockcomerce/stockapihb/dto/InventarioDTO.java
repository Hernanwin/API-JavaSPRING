package com.stockcomerce.stockapihb.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDTO {
    private Long id;

    private Long idProducto;
    private String nombreProducto;

    private Long idTienda;
    private String nombreTienda;

    private Integer cantidad;
    private Integer stockMinimo;
}
