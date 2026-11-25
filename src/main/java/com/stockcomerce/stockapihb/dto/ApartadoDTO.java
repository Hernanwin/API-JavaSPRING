package com.stockcomerce.stockapihb.dto;

import com.stockcomerce.stockapihb.models.EstadoApartado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartadoDTO {
    private Long id;
    @NotNull(message = "Debe indicar la tienda/sucursal")
    private Long idTienda;
    private String nombreTienda;
    @NotBlank(message = "Debe indicar el nombre del cliente")
    private String clienteNombre;
    private String clienteContacto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private EstadoApartado estado;
    private BigDecimal total;
    @PositiveOrZero(message = "El monto abonado no puede ser negativo")
    private BigDecimal montoAbonado;
    private BigDecimal saldoPendiente;
    private String creadoPor;
    private Long idVentaGenerada;
    @NotEmpty(message = "Debe incluir al menos un producto para apartar")
    private List<@Valid DetalleApartadoDTO> detalle;
}
