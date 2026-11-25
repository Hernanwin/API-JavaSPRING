package com.stockcomerce.stockapihb.models;

import com.stockcomerce.stockapihb.security.NivelRiesgo;
import com.stockcomerce.stockapihb.security.Permiso;
import com.stockcomerce.stockapihb.security.RolCodigo;
import com.stockcomerce.stockapihb.security.TipoAlcance;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RolCodigo codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRiesgo nivelRiesgo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlcance tipoAlcance;

    @Enumerated(EnumType.STRING)
    private TipoUbicacion tipoUbicacionRequerido;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permiso", nullable = false)
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
}
