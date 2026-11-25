package com.stockcomerce.stockapihb.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// @Getter/@Setter en vez de @Data para evitar recursion infinita con Venta (ver Venta.java)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DetalleVenta{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //muchos detalles pueden estar asociados a 1 venta.
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "VentaId")
    private Venta venta;

    //muchos detalles asociados a un mismo producto.
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "productoId")
    private Producto producto;

    private Integer cantidad;

    // se toma de Producto.precio al momento de la venta (no lo manda el cliente)
    @Column(precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    // foto del costo al momento de vender para reportes de margenes (nunca se expone al POS)
    @Column(precision = 15, scale = 2)
    private BigDecimal costoUnitarioHistorico;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;
}
