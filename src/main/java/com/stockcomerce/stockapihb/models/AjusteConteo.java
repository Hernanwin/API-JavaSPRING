package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ajuste_conteo")
public class AjusteConteo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    private Integer cantidadSistemaAnterior;
    private Integer cantidadContada;
    private Integer diferencia;
    private String motivo;
    private String ejecutadoPor;
    private LocalDateTime fecha;
}
