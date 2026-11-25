package com.stockcomerce.stockapihb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorDTO {
    private Long id;
    @NotBlank(message = "El nombre del proveedor es obligatorio")
    private String nombre;
    private String identificacionFiscal;
    private String contacto;
    private String email;
    private String telefono;
    @Builder.Default
    private Boolean activo = true;
}
