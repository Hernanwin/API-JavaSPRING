package com.stockcomerce.stockapihb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockcomerce.stockapihb.models.EstadoOrdenCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdenCompraDTO {
    private Long id;
    @NotNull(message = "Debe indicar el proveedor")
    private Long idProveedor;
    private String nombreProveedor;
    @NotNull(message = "Debe indicar la ubicacion destino")
    private Long idTiendaDestino;
    private String nombreTiendaDestino;
    private EstadoOrdenCompra estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaUltimaRecepcion;
    private BigDecimal totalEstimado;
    private Integer totalUnidadesSolicitadas;
    private Integer totalUnidadesRecibidas;
    private BigDecimal porcentajeCumplimiento;
    private String creadoPor;
    private String aprobadoPor;
    private String observacion;
    @NotEmpty(message = "Debe incluir al menos un item en la orden de compra")
    private List<@Valid DetalleOrdenCompraDTO> detalle;
}
