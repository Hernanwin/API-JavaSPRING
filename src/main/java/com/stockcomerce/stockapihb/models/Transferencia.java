package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_origen_id")
    private Tienda tiendaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_destino_id")
    private Tienda tiendaDestino;

    private Integer cantidad;
    private LocalDateTime fecha;
}
