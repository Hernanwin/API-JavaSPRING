package com.stockcomerce.stockapihb.mapper;

import com.stockcomerce.stockapihb.dto.DetalleVentaDTO;
import com.stockcomerce.stockapihb.dto.EntradaDTO;
import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.models.DetalleVenta;
import com.stockcomerce.stockapihb.models.Entrada;
import com.stockcomerce.stockapihb.models.Inventario;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Salida;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Transferencia;
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
                .build();
    }

    public static InventarioDTO inventDTO(Inventario inv) {
        if (inv == null) return null;

        return InventarioDTO.builder()
                .id(inv.getId())
                .idProducto(inv.getProducto() != null ? inv.getProducto().getId() : null)
                .nombreProducto(inv.getProducto() != null ? inv.getProducto().getNombre() : null)
                .idTienda(inv.getTienda() != null ? inv.getTienda().getId() : null)
                .nombreTienda(inv.getTienda() != null ? inv.getTienda().getNombre() : null)
                .cantidad(inv.getCantidad())
                .stockMinimo(inv.getStockMinimo())
                .build();
    }

    public static EntradaDTO entradaDTO(Entrada e) {
        if (e == null) return null;

        return EntradaDTO.builder()
                .id(e.getId())
                .idProducto(e.getProducto() != null ? e.getProducto().getId() : null)
                .nombreProducto(e.getProducto() != null ? e.getProducto().getNombre() : null)
                .idTienda(e.getTienda() != null ? e.getTienda().getId() : null)
                .nombreTienda(e.getTienda() != null ? e.getTienda().getNombre() : null)
                .cantidad(e.getCantidad())
                .fecha(e.getFecha())
                .observacion(e.getObservacion())
                .build();
    }

    public static SalidaDTO salidaDTO(Salida s) {
        if (s == null) return null;

        return SalidaDTO.builder()
                .id(s.getId())
                .idProducto(s.getProducto() != null ? s.getProducto().getId() : null)
                .nombreProducto(s.getProducto() != null ? s.getProducto().getNombre() : null)
                .idTienda(s.getTienda() != null ? s.getTienda().getId() : null)
                .nombreTienda(s.getTienda() != null ? s.getTienda().getNombre() : null)
                .idVenta(s.getVenta() != null ? s.getVenta().getId() : null)
                .cantidad(s.getCantidad())
                .fecha(s.getFecha())
                .build();
    }

    public static TransferenciaDTO transferenciaDTO(Transferencia t) {
        if (t == null) return null;

        return TransferenciaDTO.builder()
                .id(t.getId())
                .idProducto(t.getProducto() != null ? t.getProducto().getId() : null)
                .nombreProducto(t.getProducto() != null ? t.getProducto().getNombre() : null)
                .idTiendaOrigen(t.getTiendaOrigen() != null ? t.getTiendaOrigen().getId() : null)
                .nombreTiendaOrigen(t.getTiendaOrigen() != null ? t.getTiendaOrigen().getNombre() : null)
                .idTiendaDestino(t.getTiendaDestino() != null ? t.getTiendaDestino().getId() : null)
                .nombreTiendaDestino(t.getTiendaDestino() != null ? t.getTiendaDestino().getNombre() : null)
                .cantidad(t.getCantidad())
                .fecha(t.getFecha())
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
