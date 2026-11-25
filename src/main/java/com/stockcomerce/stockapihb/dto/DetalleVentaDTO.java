package com.stockcomerce.stockapihb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVentaDTO {
    private Long id; // del detalle
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto; // id del producto asociado
    private String nombreProd; // nombre del producto asociado
    @NotNull(message = "Debe indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidadProd; // cantidad de ese producto
    // solo salida: se toma de Producto.precio, lo que mande el cliente se ignora
    private BigDecimal precioUnitario;
    private BigDecimal subtotal; // sirve para sumar todos los subtotales
}
