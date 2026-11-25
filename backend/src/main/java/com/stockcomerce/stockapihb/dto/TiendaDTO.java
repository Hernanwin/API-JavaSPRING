package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.models.TipoUbicacion;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//no mapeamos etiquetas JPA, los DTO solo transfieren datos y no van a la base de datos.
public class TiendaDTO {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String direccion;
    private TipoUbicacion tipo; // TIENDA por defecto si no se envia; DEPOSITO para almacenes/centros logisticos
}
