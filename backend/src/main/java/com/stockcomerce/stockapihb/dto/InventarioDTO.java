package com.stockcomerce.stockapihb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockcomerce.stockapihb.models.TipoUbicacion;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventarioDTO {
    private Long id;

    private Long idProducto;
    private String nombreProducto;
    private Long idProveedor;
    private String nombreProveedor;

    private Long idTienda;
    private String nombreTienda;
    private TipoUbicacion tipoUbicacion;

    private Integer cantidad;
    private Integer cantidadReservada;
    private Integer cantidadDisponible;
    private Integer stockMinimo;

    private BigDecimal precioVenta;
    // Solo visibles cuando el usuario tiene Permiso.COSTOS_VER (nunca se exponen al Cajero ni Depósito)
    private BigDecimal costoUnitario;
    private BigDecimal valorCostoTotal;
}
