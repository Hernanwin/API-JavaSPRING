package com.stockcomerce.stockapihb.mapper;

import com.stockcomerce.stockapihb.dto.DetalleVentaDTO;
import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.models.DetalleVenta;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mapper {
    //mapeo de los models a DTO que es lo que en verdad queremos llamar mediante un uso estatico.
    //mapeo de Producto a ProductoDTO
    public static ProductoDTO prodDTO(Producto p) {
        if (p == null) return null;

        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .categoria(p.getCategoria())
                .precio(p.getPrecio())
                .cantidad(p.getCantidad())
                .build();
    }

    public static TiendaDTO tienDTO(Tienda t) {
        if (t == null) return null;

        return TiendaDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .direccion(t.getDireccion())
                .build();
    }
    public static VentaDTO ventDTO(Venta venta) {
        if (venta == null) return null;

        var detalle = venta.getDetalle().stream().map(det ->
                DetalleVentaDTO.builder()
                        .id(det.getId())
                        .idProducto(det.getProducto() != null ? det.getProducto().getId() : null)
                        .nombreProd(det.getProducto() != null ? det.getProducto().getNombre() : null)
                        .cantidadProd(det.getCantidad())
                        .precioUnitario(det.getPrecioUnitario())
                        .subtotal(det.getSubtotal() != null ? det.getSubtotal() : 0.0)
                        .build()
        ).collect(Collectors.toList());

        var total = detalle.stream()
                .map(DetalleVentaDTO::getSubtotal)
                .reduce(0.0, Double::sum);

        return VentaDTO.builder()
                .id(venta.getId())
                .fechaHora(venta.getFechaHora())
                .idTienda(venta.getTienda() != null ? venta.getTienda().getId() : null)
                .estado(venta.getEstado())
                .detalle(detalle)
                .total(total)
                .build();
    }
}
