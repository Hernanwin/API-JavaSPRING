package com.stockcomerce.stockapihb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {
    //datos de la venta
    private Long id;
    private LocalDateTime fechaHora; // opcional al crear: si viene null se usa la hora actual
    private String estado;           // opcional al crear: por defecto COMPLETADA
    private String caja;             // opcional: si el cajero tiene caja asignada en su rol se completa sola
    private String cajeroUsername;   // solo salida: usuario que opero el POS
    //datos de la tienda
    @NotNull(message = "Debe indicar la tienda")
    private Long idTienda;
    //listado de detalles
    @NotEmpty(message = "Debe incluir al menos un producto")
    private List<@Valid DetalleVentaDTO> detalle;
    //total de la venta en si (solo salida: se calcula sumando subtotales)
    private BigDecimal total;
}
