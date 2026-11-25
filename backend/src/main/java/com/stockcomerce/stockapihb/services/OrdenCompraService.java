package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.DetalleOrdenCompraDTO;
import com.stockcomerce.stockapihb.dto.EntradaDTO;
import com.stockcomerce.stockapihb.dto.OrdenCompraDTO;
import com.stockcomerce.stockapihb.dto.RecepcionOrdenCompraDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.models.*;
import com.stockcomerce.stockapihb.repositories.OrdenCompraRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.ProveedorRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrdenCompraService implements IOrdenCompraService {

    @Autowired
    private OrdenCompraRepository repo;
    @Autowired
    private ProveedorRepository proveedorRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private IEntradaService entradaService;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> listar(EstadoOrdenCompra estado, Long idProveedor, Long idTiendaDestino) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.ORDEN_COMPRA_VER, Permiso.ORDEN_COMPRA_APROBAR, Permiso.ENTRADA_REGISTRAR);
        if (idTiendaDestino != null && !alcance.incluye(idTiendaDestino)) {
            accessControl.exigirPermisoEnUbicacion(idTiendaDestino,
                    Permiso.ORDEN_COMPRA_VER, Permiso.ORDEN_COMPRA_APROBAR, Permiso.ENTRADA_REGISTRAR);
        }

        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findAllByOrderByFechaCreacionDescIdDesc().stream()
                .filter(oc -> alcance.incluye(oc.getTiendaDestino().getId()))
                .filter(oc -> estado == null || oc.getEstado() == estado)
                .filter(oc -> idProveedor == null || oc.getProveedor().getId().equals(idProveedor))
                .filter(oc -> idTiendaDestino == null || oc.getTiendaDestino().getId().equals(idTiendaDestino))
                .map(oc -> toDTO(oc, verCostos))
                .toList();
    }

    @Override
    @Transactional
    public OrdenCompraDTO crear(OrdenCompraDTO dto) {
        accessControl.exigirPermiso(Permiso.ORDEN_COMPRA_CREAR);
        if (dto == null) throw new IllegalArgumentException("OrdenCompraDTO obligatorio");
        if (dto.getIdProveedor() == null) throw new IllegalArgumentException("Debe indicar el proveedor");
        if (dto.getIdTiendaDestino() == null) throw new IllegalArgumentException("Debe indicar la ubicacion destino");
        if (dto.getDetalle() == null || dto.getDetalle().isEmpty())
            throw new IllegalArgumentException("Debe incluir al menos un producto");

        Proveedor proveedor = proveedorRepo.findById(dto.getIdProveedor())
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        Tienda destino = tiendaRepo.findById(dto.getIdTiendaDestino())
                .orElseThrow(() -> new NotFoundException("Ubicacion destino no encontrada"));

        OrdenCompra oc = OrdenCompra.builder()
                .proveedor(proveedor)
                .tiendaDestino(destino)
                .estado(EstadoOrdenCompra.PENDIENTE_APROBACION)
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(accessControl.usernameActual())
                .observacion(dto.getObservacion())
                .detalle(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrdenCompraDTO item : dto.getDetalle()) {
            if (item.getIdProducto() == null)
                throw new IllegalArgumentException("Cada item debe indicar idProducto");
            if (item.getCantidadSolicitada() == null || item.getCantidadSolicitada() <= 0)
                throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a 0");

            Producto prod = productoRepo.findById(item.getIdProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + item.getIdProducto()));

            BigDecimal costoUnit = item.getCostoUnitario() != null
                    ? item.getCostoUnitario()
                    : (prod.getCosto() != null ? prod.getCosto() : BigDecimal.ZERO);
            if (costoUnit.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El costo unitario no puede ser negativo");
            }
            BigDecimal sub = costoUnit.multiply(BigDecimal.valueOf(item.getCantidadSolicitada()));
            total = total.add(sub);

            oc.getDetalle().add(DetalleOrdenCompra.builder()
                    .ordenCompra(oc)
                    .producto(prod)
                    .cantidadSolicitada(item.getCantidadSolicitada())
                    .cantidadRecibida(0)
                    .costoUnitario(costoUnit)
                    .subtotal(sub)
                    .build());
        }
        oc.setTotalEstimado(total);

        return toDTO(repo.save(oc), accessControl.puedeVerCostos());
    }

    @Override
    @Transactional
    public OrdenCompraDTO aprobar(Long id) {
        accessControl.exigirPermiso(Permiso.ORDEN_COMPRA_APROBAR);
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Orden de compra no encontrada"));
        if (oc.getEstado() != EstadoOrdenCompra.PENDIENTE_APROBACION) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar ordenes en estado PENDIENTE_APROBACION (actual: " + oc.getEstado() + ")");
        }
        oc.setEstado(EstadoOrdenCompra.APROBADA);
        oc.setFechaAprobacion(LocalDateTime.now());
        oc.setAprobadoPor(accessControl.usernameActual());
        return toDTO(repo.save(oc), accessControl.puedeVerCostos());
    }

    @Override
    @Transactional
    public OrdenCompraDTO cancelar(Long id) {
        accessControl.exigirPermiso(Permiso.ORDEN_COMPRA_APROBAR);
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Orden de compra no encontrada"));
        if (oc.getEstado() == EstadoOrdenCompra.RECIBIDA_COMPLETA
                || oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL) {
            throw new IllegalStateException("No se puede cancelar una orden que ya registra recepciones");
        }
        oc.setEstado(EstadoOrdenCompra.CANCELADA);
        return toDTO(repo.save(oc), accessControl.puedeVerCostos());
    }

    @Override
    @Transactional
    public OrdenCompraDTO registrarRecepcion(Long id, RecepcionOrdenCompraDTO recepcion) {
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Orden de compra no encontrada"));

        // Debe tener permiso operativo de recepcion/entrada en la tienda o deposito destino
        accessControl.exigirPermisoEnUbicacion(oc.getTiendaDestino().getId(),
                Permiso.ENTRADA_REGISTRAR, Permiso.TRANSFERENCIA_OPERAR);

        if (oc.getEstado() != EstadoOrdenCompra.APROBADA
                && oc.getEstado() != EstadoOrdenCompra.RECIBIDA_PARCIAL) {
            throw new IllegalStateException(
                    "Solo se pueden recibir ordenes APROBADAS o RECIBIDAS_PARCIALMENTE (actual: " + oc.getEstado() + ")");
        }

        Map<Long, Integer> cantidadesARecibir = new LinkedHashMap<>();
        if (recepcion == null || recepcion.getItems() == null || recepcion.getItems().isEmpty()) {
            for (DetalleOrdenCompra det : oc.getDetalle()) {
                int pendiente = det.getCantidadSolicitada() - det.getCantidadRecibida();
                if (pendiente > 0) {
                    cantidadesARecibir.put(det.getProducto().getId(), pendiente);
                }
            }
        } else {
            for (RecepcionOrdenCompraDTO.ItemRecepcion it : recepcion.getItems()) {
                if (it.getCantidadRecibida() == null || it.getCantidadRecibida() <= 0)
                    throw new IllegalArgumentException("La cantidad recibida debe ser mayor a 0");
                cantidadesARecibir.merge(it.getIdProducto(), it.getCantidadRecibida(), Integer::sum);
            }
        }

        if (cantidadesARecibir.isEmpty()) {
            throw new IllegalStateException("La orden no tiene unidades pendientes de recepcion");
        }

        String obsBase = "Recepcion OC #" + oc.getId()
                + (recepcion != null && recepcion.getObservacion() != null ? " - " + recepcion.getObservacion() : "");

        for (Map.Entry<Long, Integer> entry : cantidadesARecibir.entrySet()) {
            DetalleOrdenCompra det = oc.getDetalle().stream()
                    .filter(d -> d.getProducto().getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto id=" + entry.getKey() + " no pertenece a la orden de compra"));
            int nuevaRecibida = det.getCantidadRecibida() + entry.getValue();
            if (nuevaRecibida > det.getCantidadSolicitada()) {
                throw new IllegalArgumentException(
                        "La cantidad recibida supera lo solicitado para el producto '" + det.getProducto().getNombre() + "'");
            }
            det.setCantidadRecibida(nuevaRecibida);

            entradaService.crearEntrada(EntradaDTO.builder()
                    .idProducto(det.getProducto().getId())
                    .idTienda(oc.getTiendaDestino().getId())
                    .cantidad(entry.getValue())
                    .observacion(obsBase)
                    .build());
        }

        boolean completa = oc.getDetalle().stream()
                .allMatch(d -> d.getCantidadRecibida() >= d.getCantidadSolicitada());
        oc.setEstado(completa ? EstadoOrdenCompra.RECIBIDA_COMPLETA : EstadoOrdenCompra.RECIBIDA_PARCIAL);
        oc.setFechaUltimaRecepcion(LocalDateTime.now());

        return toDTO(repo.save(oc), accessControl.puedeVerCostos());
    }

    private OrdenCompraDTO toDTO(OrdenCompra oc, boolean incluirCostos) {
        int totalSol = 0;
        int totalRec = 0;
        List<DetalleOrdenCompraDTO> items = new ArrayList<>();
        for (DetalleOrdenCompra d : oc.getDetalle()) {
            totalSol += d.getCantidadSolicitada();
            totalRec += d.getCantidadRecibida();
            items.add(DetalleOrdenCompraDTO.builder()
                    .id(d.getId())
                    .idProducto(d.getProducto().getId())
                    .nombreProducto(d.getProducto().getNombre())
                    .cantidadSolicitada(d.getCantidadSolicitada())
                    .cantidadRecibida(d.getCantidadRecibida())
                    .costoUnitario(incluirCostos ? d.getCostoUnitario() : null)
                    .subtotal(incluirCostos ? d.getSubtotal() : null)
                    .build());
        }
        BigDecimal cumplimiento = totalSol > 0
                ? BigDecimal.valueOf(totalRec * 100.0 / totalSol).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OrdenCompraDTO.builder()
                .id(oc.getId())
                .idProveedor(oc.getProveedor().getId())
                .nombreProveedor(oc.getProveedor().getNombre())
                .idTiendaDestino(oc.getTiendaDestino().getId())
                .nombreTiendaDestino(oc.getTiendaDestino().getNombre())
                .estado(oc.getEstado())
                .fechaCreacion(oc.getFechaCreacion())
                .fechaAprobacion(oc.getFechaAprobacion())
                .fechaUltimaRecepcion(oc.getFechaUltimaRecepcion())
                .totalEstimado(incluirCostos ? oc.getTotalEstimado() : null)
                .totalUnidadesSolicitadas(totalSol)
                .totalUnidadesRecibidas(totalRec)
                .porcentajeCumplimiento(cumplimiento)
                .creadoPor(oc.getCreadoPor())
                .aprobadoPor(oc.getAprobadoPor())
                .observacion(oc.getObservacion())
                .detalle(items)
                .build();
    }
}
