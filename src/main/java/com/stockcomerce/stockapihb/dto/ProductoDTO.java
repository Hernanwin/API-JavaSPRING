package com.stockcomerce.stockapihb.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductoDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private String categoria;
    // el stock ahora se consulta por /api/inventario (Producto + Tienda), no por aca
}