package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String categoria;
    @Column(precision = 15, scale = 2)
    private BigDecimal precio;

    // Costo de compra/reposicion: sensible, oculto a roles sin Permiso.COSTOS_VER (ej. Cajero/Deposito)
    @Column(precision = 15, scale = 2)
    private BigDecimal costo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    // el stock ya no vive aca: ahora es Inventario (Producto + Tienda)
}
