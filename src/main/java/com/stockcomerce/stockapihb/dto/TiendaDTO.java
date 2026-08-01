package com.stockcomerce.stockapihb.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//no mapeamos etiquetas JPA, los DTO solo transfieren datos y no van a la base de datos.
public class TiendaDTO {
    private Long id;
    private String nombre;
    private String direccion;
}