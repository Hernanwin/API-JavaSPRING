package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidadSolicitada;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidadRecibida = 0;

    @Column(precision = 15, scale = 2)
    private BigDecimal costoUnitario;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;
}
