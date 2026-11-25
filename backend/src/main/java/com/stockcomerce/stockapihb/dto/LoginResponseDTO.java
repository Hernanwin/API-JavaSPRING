package com.stockcomerce.stockapihb.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private long expiracionSegundos;
    private UsuarioDTO usuario;
}
