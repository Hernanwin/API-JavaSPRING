package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ApartadoDTO;
import com.stockcomerce.stockapihb.dto.DetalleApartadoDTO;
import com.stockcomerce.stockapihb.dto.DetalleVentaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.models.*;
import com.stockcomerce.stockapihb.repositories.ApartadoRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
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
public class ApartadoService implements IApartadoService {

    @Autowired
    private ApartadoRepository repo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private IInventarioService inventarioService;
    @Autowired
    private IVentaService ventaService;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<ApartadoDTO> listar(Long idTienda, EstadoApartado estado) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.APARTADO_GESTIONAR, Permiso.VENTA_ADMINISTRAR);
        if (idTienda != null && !alcance.incluye(idTienda)) {
            accessControl.exigirPermisoEnUbicacion(idTienda,
                    Permiso.APARTADO_GESTIONAR, Permiso.VENTA_ADMINISTRAR);
        }
        return repo.findAllByOrderByFechaCreacionDescIdDesc().stream()
                .filter(a -> alcance.incluye(a.getTienda().getId()))
                .filter(a -> idTienda == null || a.getTienda().getId().equals(idTienda))
                .filter(a -> estado == null || a.getEstado() == estado)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ApartadoDTO crear(ApartadoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ApartadoDTO obligatorio");
        if (dto.getIdTienda() == null) throw new IllegalArgumentException("Debe indicar la tienda");
        if (dto.getClienteNombre() == null || dto.getClienteNombre().isBlank())
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        if (dto.getDetalle() == null || dto.getDetalle().isEmpty())
            throw new IllegalArgumentException("Debe incluir al menos un producto");

        accessControl.exigirPermisoEnUbicacion(dto.getIdTienda(), Permiso.APARTADO_GESTIONAR);

        Tienda tienda = tiendaRepo.findById(dto.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Tienda no encontrada"));

        Apartado apartado = Apartado.builder()
                .tienda(tienda)
                .clienteNombre(dto.getClienteNombre().trim())
                .clienteContacto(dto.getClienteContacto())
                .fechaCreacion(LocalDateTime.now())
                .fechaVencimiento(dto.getFechaVencimiento() != null
                        ? dto.getFechaVencimiento()
                        : LocalDateTime.now().plusDays(15))
                .estado(EstadoApartado.ACTIVO)
                .creadoPor(accessControl.usernameActual())
                .detalle(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleApartadoDTO item : dto.getDetalle()) {
            if (item.getIdProducto() == null)
                throw new IllegalArgumentException("Cada item debe indicar idProducto");
            if (item.getCantidad() == null || item.getCantidad() <= 0)
                throw new IllegalArgumentException("La cantidad apartada debe ser mayor a 0");

            Producto prod = productoRepo.findById(item.getIdProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + item.getIdProducto()));
            if (prod.getPrecio() == null) {
                throw new IllegalStateException("El producto '" + prod.getNombre() + "' no tiene precio de venta");
            }

            // Reserva el stock en la tienda para que no pueda venderse a otro cliente mientras este activo
            inventarioService.reservarStock(prod.getId(), tienda.getId(), item.getCantidad());

            BigDecimal sub = prod.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(sub);

            apartado.getDetalle().add(DetalleApartado.builder()
                    .apartado(apartado)
                    .producto(prod)
                    .cantidad(item.getCantidad())
                    .precioUnitario(prod.getPrecio())
                    .subtotal(sub)
                    .build());
        }

        BigDecimal abonado = dto.getMontoAbonado() != null ? dto.getMontoAbonado() : BigDecimal.ZERO;
        if (abonado.compareTo(BigDecimal.ZERO) < 0 || abonado.compareTo(total) > 0) {
            throw new IllegalArgumentException("El monto abonado debe estar entre 0 y el total del apartado");
        }
        apartado.setTotal(total);
        apartado.setMontoAbonado(abonado);
        apartado.setSaldoPendiente(total.subtract(abonado));

        return toDTO(repo.save(apartado));
    }

    @Override
    @Transactional
    public ApartadoDTO cobrar(Long id) {
        Apartado apartado = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Apartado no encontrado"));

        Long idTienda = apartado.getTienda().getId();
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.APARTADO_GESTIONAR);
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.POS_COBRAR);

        if (apartado.getEstado() != EstadoApartado.ACTIVO) {
            throw new IllegalStateException("Solo se pueden cobrar apartados ACTIVOS (actual: " + apartado.getEstado() + ")");
        }

        // Libera la reserva e inmediatamente registra la Venta definitiva (descontando stock fisico)
        List<DetalleVentaDTO> lineasVenta = new ArrayList<>();
        for (DetalleApartado det : apartado.getDetalle()) {
            inventarioService.liberarReserva(det.getProducto().getId(), idTienda, det.getCantidad());
            lineasVenta.add(DetalleVentaDTO.builder()
                    .idProducto(det.getProducto().getId())
                    .cantidadProd(det.getCantidad())
                    .build());
        }

        VentaDTO venta = ventaService.crearVenta(VentaDTO.builder()
                .idTienda(idTienda)
                .detalle(lineasVenta)
                .build());

        apartado.setEstado(EstadoApartado.COMPLETADO);
        apartado.setMontoAbonado(apartado.getTotal());
        apartado.setSaldoPendiente(BigDecimal.ZERO);
        apartado.setIdVentaGenerada(venta.getId());

        return toDTO(repo.save(apartado));
    }

    @Override
    @Transactional
    public ApartadoDTO cancelar(Long id) {
        Apartado apartado = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Apartado no encontrado"));

        Long idTienda = apartado.getTienda().getId();
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.APARTADO_GESTIONAR);

        if (apartado.getEstado() != EstadoApartado.ACTIVO) {
            throw new IllegalStateException("Solo se pueden cancelar apartados ACTIVOS (actual: " + apartado.getEstado() + ")");
        }

        for (DetalleApartado det : apartado.getDetalle()) {
            inventarioService.liberarReserva(det.getProducto().getId(), idTienda, det.getCantidad());
        }
        apartado.setEstado(EstadoApartado.CANCELADO);
        return toDTO(repo.save(apartado));
    }

    private ApartadoDTO toDTO(Apartado a) {
        List<DetalleApartadoDTO> items = a.getDetalle().stream()
                .map(d -> DetalleApartadoDTO.builder()
                        .id(d.getId())
                        .idProducto(d.getProducto().getId())
                        .nombreProducto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        return ApartadoDTO.builder()
                .id(a.getId())
                .idTienda(a.getTienda().getId())
                .nombreTienda(a.getTienda().getNombre())
                .clienteNombre(a.getClienteNombre())
                .clienteContacto(a.getClienteContacto())
                .fechaCreacion(a.getFechaCreacion())
                .fechaVencimiento(a.getFechaVencimiento())
                .estado(a.getEstado())
                .total(a.getTotal())
                .montoAbonado(a.getMontoAbonado())
                .saldoPendiente(a.getSaldoPendiente())
                .creadoPor(a.getCreadoPor())
                .idVentaGenerada(a.getIdVentaGenerada())
                .detalle(items)
                .build();
    }
}
