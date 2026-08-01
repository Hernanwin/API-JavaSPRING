package com.stockcomerce.stockapihb.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DetalleVenta{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    //muchos detalles pueden estar asociados a 1 venta.
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "VentaId")
    private Venta venta;

    //muchos detalles asociados a un mismo producto.
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "productoId")
    private Producto producto;

    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}