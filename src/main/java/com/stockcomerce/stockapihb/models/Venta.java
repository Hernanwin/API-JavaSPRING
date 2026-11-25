package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Getter/@Setter en vez de @Data: @Data genera equals/hashCode/toString que recorren
// la relacion bidireccional Venta <-> DetalleVenta y terminan en StackOverflowError.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaHora;
    private String estado;
    private String caja;
    private String cajeroUsername;

    // dinero en BigDecimal, igual que Producto.precio. Se calcula como suma de subtotales.
    @Column(precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "tienda_id")
    private Tienda tienda;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleVenta> detalle = new ArrayList<>();
}
