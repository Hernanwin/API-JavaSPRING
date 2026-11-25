package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.DetalleVentaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.DetalleVenta;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.repositories.VentaRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService implements IVentaService {

    public static final String ESTADO_POR_DEFECTO = "COMPLETADA";

    @Autowired
    private VentaRepository ventareposi;
    @Autowired
    private ProductoRepository productoreposi;
    @Autowired
    private TiendaRepository tiendareposi;
    @Autowired
    private ISalidaService salidaService;
    @Autowired
    private AccessControlService accessControl;

    @Transactional(readOnly = true)
    @Override
    public List<VentaDTO> mostrarVentas() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.POS_COBRAR, Permiso.VENTA_ADMINISTRAR, Permiso.REPORTE_FINANCIERO_CONSOLIDADO_VER);
        return ventareposi.findAll().stream()
                .filter(v -> v.getTienda() != null && alcance.incluye(v.getTienda().getId()))
                .map(Mapper::ventDTO)
                .toList();
    }

    @Transactional
    @Override
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        if (ventaDTO == null) throw new IllegalArgumentException("VentaDTO no existe");
        if (ventaDTO.getIdTienda() == null) throw new IllegalArgumentException("Debe indicar la tienda");
        if (ventaDTO.getDetalle() == null || ventaDTO.getDetalle().isEmpty())
            throw new IllegalArgumentException("Debe incluir al menos un producto");

        accessControl.exigirPermisoEnUbicacion(ventaDTO.getIdTienda(), Permiso.POS_COBRAR);

        Tienda tienda = tiendareposi.findById(ventaDTO.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Tienda no localizada"));

        Venta venta = new Venta();
        venta.setFechaHora(ventaDTO.getFechaHora() != null ? ventaDTO.getFechaHora() : LocalDateTime.now());
        venta.setEstado(ventaDTO.getEstado() != null && !ventaDTO.getEstado().isBlank()
                ? ventaDTO.getEstado() : ESTADO_POR_DEFECTO);
        venta.setTienda(tienda);
        String caja = (ventaDTO.getCaja() != null && !ventaDTO.getCaja().isBlank())
                ? ventaDTO.getCaja()
                : accessControl.cajaAsignadaEn(tienda.getId()).orElse(null);
        venta.setCaja(caja);
        venta.setCajeroUsername(accessControl.usernameActual());

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVentaDTO detalleVentaDTO : ventaDTO.getDetalle()) {
            if (detalleVentaDTO.getIdProducto() == null)
                throw new IllegalArgumentException("Cada detalle debe indicar el producto");
            if (detalleVentaDTO.getCantidadProd() == null || detalleVentaDTO.getCantidadProd() <= 0)
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a 0");

            Producto p = productoreposi.findById(detalleVentaDTO.getIdProducto())
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado: " + detalleVentaDTO.getIdProducto()));
            if (p.getPrecio() == null)
                throw new IllegalStateException("El producto '" + p.getNombre() + "' no tiene precio");

            BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(detalleVentaDTO.getCantidadProd()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(p);
            detalle.setCantidad(detalleVentaDTO.getCantidadProd());
            detalle.setPrecioUnitario(p.getPrecio());
            detalle.setCostoUnitarioHistorico(p.getCosto());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
            total = total.add(subtotal);
        }
        venta.setDetalle(detalles);
        venta.setTotal(total);
        venta = ventareposi.save(venta);

        for (DetalleVenta detalle : venta.getDetalle()) {
            salidaService.registrarSalidaPorVenta(venta, detalle.getProducto(), tienda, detalle.getCantidad());
        }

        return Mapper.ventDTO(venta);
    }

    @Transactional
    @Override
    public VentaDTO actualizarVenta(Long id, VentaDTO ventaDTO) {
        Venta venta = ventareposi.findById(id)
                .orElseThrow(() -> new NotFoundException("La venta no se encontro"));

        if (venta.getTienda() != null) {
            accessControl.exigirPermisoEnUbicacion(venta.getTienda().getId(), Permiso.VENTA_ADMINISTRAR);
        } else {
            accessControl.exigirPermiso(Permiso.VENTA_ADMINISTRAR);
        }

        if (ventaDTO.getDetalle() != null && !ventaDTO.getDetalle().isEmpty()) {
            throw new IllegalArgumentException(
                    "El detalle de una venta no se puede modificar; elimine la venta y cree una nueva");
        }
        if (ventaDTO.getFechaHora() != null) {
            venta.setFechaHora(ventaDTO.getFechaHora());
        }
        if (ventaDTO.getEstado() != null && !ventaDTO.getEstado().isBlank()) {
            venta.setEstado(ventaDTO.getEstado());
        }
        if (ventaDTO.getIdTienda() != null
                && (venta.getTienda() == null || !ventaDTO.getIdTienda().equals(venta.getTienda().getId()))) {
            accessControl.exigirPermisoEnUbicacion(ventaDTO.getIdTienda(), Permiso.VENTA_ADMINISTRAR);
            Tienda nueva = tiendareposi.findById(ventaDTO.getIdTienda())
                    .orElseThrow(() -> new NotFoundException("Tienda no localizada"));
            salidaService.reasignarTiendaDeVenta(venta, nueva);
            venta.setTienda(nueva);
        }

        return Mapper.ventDTO(ventareposi.save(venta));
    }

    @Transactional
    @Override
    public void eliminarVenta(Long id) {
        Venta venta = ventareposi.findById(id)
                .orElseThrow(() -> new NotFoundException("La venta no existe"));
        if (venta.getTienda() != null) {
            accessControl.exigirPermisoEnUbicacion(venta.getTienda().getId(), Permiso.VENTA_ADMINISTRAR);
        } else {
            accessControl.exigirPermiso(Permiso.VENTA_ADMINISTRAR);
        }
        salidaService.revertirSalidasDeVenta(venta);
        ventareposi.delete(venta);
    }
}
