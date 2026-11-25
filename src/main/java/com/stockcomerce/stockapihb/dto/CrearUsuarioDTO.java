package com.stockcomerce.stockapihb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearUsuarioDTO {
    @NotBlank(message = "El username es obligatorio")
    private String username;
    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    @NotEmpty(message = "Debe asignar al menos un rol al usuario")
    private List<@Valid AsignacionRolDTO> asignaciones;
}
