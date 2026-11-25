package com.stockcomerce.stockapihb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionOrdenCompraDTO {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRecepcion {
        @NotNull(message = "Debe indicar el producto recibido")
        private Long idProducto;
        @NotNull(message = "Debe indicar la cantidad recibida")
        @Positive(message = "La cantidad recibida debe ser mayor a 0")
        private Integer cantidadRecibida;
    }

    // Opcional: si no se envia o viene vacio, recibe la totalidad pendiente de la orden aprobada
    private List<@Valid ItemRecepcion> items;
    private String observacion;
}
