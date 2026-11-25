package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Salida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id")
    private Tienda tienda;

    // Si proviene de POS apunta a la Venta; si es salida operativa de deposito es null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    private Integer cantidad;
    private LocalDateTime fecha;
    private String motivo; // VENTA, MERMA, ROTURA, CONSUMO_INTERNO, DESPACHO, etc.
    private String observacion;
    private String registradoPor;
}

