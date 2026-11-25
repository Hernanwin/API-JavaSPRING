package com.stockcomerce.stockapihb.mapper;

import com.stockcomerce.stockapihb.dto.*;
import com.stockcomerce.stockapihb.models.*;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class Mapper {

    public static ProductoDTO prodDTO(Producto p) {
        return prodDTO(p, true);
    }

    public static ProductoDTO prodDTO(Producto p, boolean incluirCostos) {
        if (p == null) return null;

        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .categoria(p.getCategoria())
                .precio(p.getPrecio())
                .costo(incluirCostos ? p.getCosto() : null)
                .idProveedor(p.getProveedor() != null ? p.getProveedor().getId() : null)
                .nombreProveedor(p.getProveedor() != null ? p.getProveedor().getNombre() : null)
                .build();
    }

    public static InventarioDTO inventDTO(Inventario inv) {
        return inventDTO(inv, true);
    }

    public static InventarioDTO inventDTO(Inventario inv, boolean incluirCostos) {
        if (inv == null) return null;

        Producto p = inv.getProducto();
        Tienda t = inv.getTienda();
        int cant = inv.getCantidad() != null ? inv.getCantidad() : 0;
        int res = inv.getCantidadReservada() != null ? inv.getCantidadReservada() : 0;

        BigDecimal costoUnit = (incluirCostos && p != null) ? p.getCosto() : null;
        BigDecimal valorTotal = (costoUnit != null)
                ? costoUnit.multiply(BigDecimal.valueOf(cant))
                : null;

        return InventarioDTO.builder()
                .id(inv.getId())
                .idProducto(p != null ? p.getId() : null)
                .nombreProducto(p != null ? p.getNombre() : null)
                .idProveedor(p != null && p.getProveedor() != null ? p.getProveedor().getId() : null)
                .nombreProveedor(p != null && p.getProveedor() != null ? p.getProveedor().getNombre() : null)
                .idTienda(t != null ? t.getId() : null)
                .nombreTienda(t != null ? t.getNombre() : null)
                .tipoUbicacion(t != null ? t.getTipo() : null)
                .cantidad(cant)
                .cantidadReservada(res)
                .cantidadDisponible(inv.getCantidadDisponible())
                .stockMinimo(inv.getStockMinimo())
                .precioVenta(p != null ? p.getPrecio() : null)
                .costoUnitario(costoUnit)
                .valorCostoTotal(valorTotal)
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
                .motivo(s.getMotivo())
                .observacion(s.getObservacion())
                .registradoPor(s.getRegistradoPor())
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
                .cantidadEnviada(t.getCantidadEnviada())
                .cantidadRecibida(t.getCantidadRecibida())
                .estado(t.getEstado())
                .fecha(t.getFecha())
                .fechaAprobacion(t.getFechaAprobacion())
                .fechaEnvio(t.getFechaEnvio())
                .fechaRecepcion(t.getFechaRecepcion())
                .solicitadoPor(t.getSolicitadoPor())
                .aprobadoPor(t.getAprobadoPor())
                .enviadoPor(t.getEnviadoPor())
                .recibidoPor(t.getRecibidoPor())
                .observacion(t.getObservacion())
                .build();
    }

    public static TiendaDTO tienDTO(Tienda t) {
        if (t == null) return null;

        return TiendaDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .direccion(t.getDireccion())
                .tipo(t.getTipo() != null ? t.getTipo() : TipoUbicacion.TIENDA)
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
                        .subtotal(det.getSubtotal() != null ? det.getSubtotal() : BigDecimal.ZERO)
                        .build()
        ).collect(Collectors.toList());

        var total = detalle.stream()
                .map(DetalleVentaDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VentaDTO.builder()
                .id(venta.getId())
                .fechaHora(venta.getFechaHora())
                .idTienda(venta.getTienda() != null ? venta.getTienda().getId() : null)
                .estado(venta.getEstado())
                .caja(venta.getCaja())
                .cajeroUsername(venta.getCajeroUsername())
                .detalle(detalle)
                .total(total)
                .build();
    }

    public static NotificacionDTO notifDTO(Notificacion n) {
        if (n == null) return null;
        return NotificacionDTO.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .idProducto(n.getIdProducto())
                .nombreProducto(n.getNombreProducto())
                .idTienda(n.getIdTienda())
                .nombreTienda(n.getNombreTienda())
                .cantidadActual(n.getCantidadActual())
                .stockMinimo(n.getStockMinimo())
                .mensaje(n.getMensaje())
                .fecha(n.getFecha())
                .leida(n.isLeida())
                .build();
    }

    public static ProveedorDTO proveedorDTO(Proveedor p) {
        if (p == null) return null;
        return ProveedorDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .identificacionFiscal(p.getIdentificacionFiscal())
                .contacto(p.getContacto())
                .email(p.getEmail())
                .telefono(p.getTelefono())
                .activo(p.isActivo())
                .build();
    }
}
