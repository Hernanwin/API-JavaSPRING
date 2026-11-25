package com.stockcomerce.stockapihb.models;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String identificacionFiscal;
    private String contacto;
    private String email;
    private String telefono;

    @Builder.Default
    private boolean activo = true;
}
