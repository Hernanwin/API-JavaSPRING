package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.models.EstadoTransferencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaDTO {
    private Long id;
    @NotNull(message = "Debe indicar el producto")
    private Long idProducto;
    private String nombreProducto;
    @NotNull(message = "Debe indicar la tienda origen")
    private Long idTiendaOrigen;
    private String nombreTiendaOrigen;
    @NotNull(message = "Debe indicar la tienda destino")
    private Long idTiendaDestino;
    private String nombreTiendaDestino;
    @NotNull(message = "Debe indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    private Integer cantidadEnviada;
    private Integer cantidadRecibida;
    private EstadoTransferencia estado;
    private LocalDateTime fecha;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRecepcion;
    private String solicitadoPor;
    private String aprobadoPor;
    private String enviadoPor;
    private String recibidoPor;
    private String observacion;
}
