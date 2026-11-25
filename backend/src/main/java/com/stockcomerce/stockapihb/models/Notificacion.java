package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Alerta generada cuando el stock de un Producto en una Tienda baja del stock minimo.
// Guarda una "foto" de los datos (ids + nombres) al momento de la alerta, sin FK,
// para que el historial no dependa de que el producto o la tienda sigan existiendo.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // por ahora solo STOCK_BAJO

    private Long idProducto;
    private String nombreProducto;
    private Long idTienda;
    private String nombreTienda;

    private Integer cantidadActual;
    private Integer stockMinimo;

    private String mensaje;
    private LocalDateTime fecha;

    @Builder.Default
    private boolean leida = false;
}
