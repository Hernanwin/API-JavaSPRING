package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

// Vinculo contextual Usuario + Rol + Ubicacion (o null cuando el rol tiene alcance GLOBAL).
// Permite que un mismo usuario opere en varias sucursales/depositos con responsabilidades precisas.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asignacion_rol")
public class AsignacionRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // Obligatoria cuando rol.tipoAlcance == UBICACION; null cuando es GLOBAL
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tienda_id")
    private Tienda tienda;

    // Identificador opcional de terminal/caja POS en esa sucursal (ej. "CAJA-01")
    private String caja;

    @Builder.Default
    private boolean activo = true;
}
