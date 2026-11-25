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
    private Integer cantidadEnviada;
    private Integer cantidadRecibida;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoTransferencia estado = EstadoTransferencia.COMPLETADA;

    private LocalDateTime fecha;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRecepcion;

    private String solicitadoPor;
    private String aprobadoPor;
    private String enviadoPor;
    private String recibidoPor;
    private String observacion;
}
