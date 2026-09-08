package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

// El stock vive aca: es la combinacion Producto + Tienda, no una propiedad del Producto.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "tienda_id"}))
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id")
    private Tienda tienda;

    @Builder.Default
    private Integer cantidad = 0;

    private Integer stockMinimo;
}

