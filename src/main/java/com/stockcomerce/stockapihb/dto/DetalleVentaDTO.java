package com.stockcomerce.stockapihb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DetalleVentaDTO {
    private Long id; // del detalle
    private Long idProducto; // id del producto asociado
    private String nombreProd; // nombre del producto asociado
    private Integer cantidadProd; // cantidad de ese producto
    private Double precioUnitario;
    private Double subtotal; // sirve para sumar todos los subtotales
}