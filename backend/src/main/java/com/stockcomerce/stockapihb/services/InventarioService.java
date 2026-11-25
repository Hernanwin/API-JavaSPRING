package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioConsolidadoDTO;
import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.events.StockBajoEvent;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Inventario;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.repositories.InventarioRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class InventarioService implements IInventarioService {

    @Autowired
    private InventarioRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private AccessControlService accessControl;

    @Value("${stock.minimo-por-defecto:12}")
    private int stockMinimoPorDefecto;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarInventario() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.INVENTARIO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findAll().stream()
                .filter(inv -> inv.getTienda() != null && alcance.incluye(inv.getTienda().getId()))
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioConsolidadoDTO> mostrarConsolidado(Long idProveedor) {
        accessControl.exigirAlcanceGlobal(Permiso.INVENTARIO_VER);
        boolean verCostos = accessControl.puedeVerCostos();

        Map<Long, List<Inventario>> porProducto = new LinkedHashMap<>();
        for (Inventario inv : repo.findAll()) {
            Producto p = inv.getProducto();
            if (p == null) continue;
            if (idProveedor != null && (p.getProveedor() == null || !idProveedor.equals(p.getProveedor().getId()))) {
                continue;
            }
            porProducto.computeIfAbsent(p.getId(), k -> new ArrayList<>()).add(inv);
        }

        List<InventarioConsolidadoDTO> resultado = new ArrayList<>();
        for (List<Inventario> filas : porProducto.values()) {
            Producto p = filas.get(0).getProducto();
            int totalCant = 0;
            int totalRes = 0;
            int tiendasConStock = 0;
            for (Inventario f : filas) {
                int c = f.getCantidad() != null ? f.getCantidad() : 0;
                int r = f.getCantidadReservada() != null ? f.getCantidadReservada() : 0;
                totalCant += c;
                totalRes += r;
                if (c > 0) tiendasConStock++;
            }
            BigDecimal costo = verCostos ? p.getCosto() : null;
            BigDecimal valorCosto = (costo != null) ? costo.multiply(BigDecimal.valueOf(totalCant)) : null;

            resultado.add(InventarioConsolidadoDTO.builder()
                    .idProducto(p.getId())
                    .nombreProducto(p.getNombre())
                    .categoria(p.getCategoria())
                    .idProveedor(p.getProveedor() != null ? p.getProveedor().getId() : null)
                    .nombreProveedor(p.getProveedor() != null ? p.getProveedor().getNombre() : null)
                    .totalUnidades(totalCant)
                    .totalReservadas(totalRes)
                    .totalDisponibles(Math.max(0, totalCant - totalRes))
                    .ubicacionesConStock(tiendasConStock)
                    .precioVenta(p.getPrecio())
                    .costoUnitario(costo)
                    .valorTotalCosto(valorCosto)
                    .build());
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarPorProveedor(Long idProveedor) {
        accessControl.exigirAlcanceGlobal(Permiso.INVENTARIO_VER);
        accessControl.exigirPermiso(Permiso.PROVEEDOR_GESTIONAR, Permiso.ORDEN_COMPRA_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findAll().stream()
                .filter(inv -> inv.getProducto() != null
                        && inv.getProducto().getProveedor() != null
                        && idProveedor.equals(inv.getProducto().getProveedor().getId()))
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarPorTienda(Long idTienda) {
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.INVENTARIO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findByTiendaId(idTienda).stream()
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarPorProducto(Long idProducto) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.INVENTARIO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findByProductoId(idProducto).stream()
                .filter(inv -> inv.getTienda() != null && alcance.incluye(inv.getTienda().getId()))
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioDTO consultarStock(Long idProducto, Long idTienda) {
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.INVENTARIO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .orElse(InventarioDTO.builder()
                        .idProducto(idProducto)
                        .idTienda(idTienda)
                        .cantidad(0)
                        .cantidadReservada(0)
                        .cantidadDisponible(0)
                        .stockMinimo(stockMinimoPorDefecto)
                        .build());
    }

    @Override
    @Transactional
    public Inventario incrementarStock(Long idProducto, Long idTienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        Inventario inv = obtenerOCrear(idProducto, idTienda);
        inv.setCantidad(inv.getCantidad() + cantidad);
        return repo.save(inv);
    }

    @Override
    @Transactional
    public Inventario decrementarStock(Long idProducto, Long idTienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        Inventario inv = repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .orElseThrow(() -> new NotFoundException(
                        "No hay stock registrado para ese producto en esa tienda"));

        int anterior = inv.getCantidad() != null ? inv.getCantidad() : 0;
        int reservada = inv.getCantidadReservada() != null ? inv.getCantidadReservada() : 0;
        int disponible = inv.getCantidadDisponible();
        if (disponible < cantidad) {
            throw new IllegalStateException(
                    reservada > 0
                            ? "Stock disponible insuficiente: hay " + disponible + " disponibles ("
                                    + reservada + " reservadas en apartados), se pidieron " + cantidad
                            : "Stock insuficiente: hay " + anterior + ", se pidieron " + cantidad);
        }
        int nueva = anterior - cantidad;
        inv.setCantidad(nueva);
        Inventario guardado = repo.save(inv);

        publicarAlertaSiCruzaMinimo(inv, anterior, nueva);
        return guardado;
    }

    @Override
    @Transactional
    public Inventario fijarStockPorConteo(Long idProducto, Long idTienda, int cantidadContada) {
        if (cantidadContada < 0) throw new IllegalArgumentException("La cantidad contada no puede ser negativa");
        Inventario inv = obtenerOCrear(idProducto, idTienda);
        int reservada = inv.getCantidadReservada() != null ? inv.getCantidadReservada() : 0;
        if (cantidadContada < reservada) {
            throw new IllegalStateException(
                    "El conteo (" + cantidadContada + ") es menor a las unidades reservadas en apartados (" + reservada + ")");
        }
        int anterior = inv.getCantidad() != null ? inv.getCantidad() : 0;
        inv.setCantidad(cantidadContada);
        Inventario guardado = repo.save(inv);
        publicarAlertaSiCruzaMinimo(inv, anterior, cantidadContada);
        return guardado;
    }

    @Override
    @Transactional
    public Inventario reservarStock(Long idProducto, Long idTienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a 0");
        Inventario inv = repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .orElseThrow(() -> new NotFoundException(
                        "No hay stock registrado para ese producto en esa tienda"));
        if (inv.getCantidadDisponible() < cantidad) {
            throw new IllegalStateException(
                    "Stock disponible insuficiente para apartar: disponibles "
                            + inv.getCantidadDisponible() + ", solicitadas " + cantidad);
        }
        int res = inv.getCantidadReservada() != null ? inv.getCantidadReservada() : 0;
        inv.setCantidadReservada(res + cantidad);
        return repo.save(inv);
    }

    @Override
    @Transactional
    public Inventario liberarReserva(Long idProducto, Long idTienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a liberar debe ser mayor a 0");
        Inventario inv = repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .orElseThrow(() -> new NotFoundException("Inventario no encontrado"));
        int res = inv.getCantidadReservada() != null ? inv.getCantidadReservada() : 0;
        inv.setCantidadReservada(Math.max(0, res - cantidad));
        return repo.save(inv);
    }

    private void publicarAlertaSiCruzaMinimo(Inventario inv, int anterior, int nueva) {
        int minimo = inv.getStockMinimo() != null ? inv.getStockMinimo() : stockMinimoPorDefecto;
        if (anterior >= minimo && nueva < minimo) {
            eventPublisher.publishEvent(new StockBajoEvent(
                    inv.getProducto().getId(), inv.getProducto().getNombre(),
                    inv.getTienda().getId(), inv.getTienda().getNombre(),
                    nueva, minimo));
        }
    }

    private Inventario obtenerOCrear(Long idProducto, Long idTienda) {
        return repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .orElseGet(() -> {
                    Producto producto = productoRepo.findById(idProducto)
                            .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
                    Tienda tienda = tiendaRepo.findById(idTienda)
                            .orElseThrow(() -> new NotFoundException("Tienda no encontrada"));
                    Inventario nuevo = Inventario.builder()
                            .producto(producto)
                            .tienda(tienda)
                            .cantidad(0)
                            .cantidadReservada(0)
                            .stockMinimo(stockMinimoPorDefecto)
                            .build();
                    return repo.save(nuevo);
                });
    }

    @Override
    @Transactional
    public InventarioDTO actualizarStockMinimo(Long idProducto, Long idTienda, Integer stockMinimo) {
        accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.STOCK_MINIMO_CONFIGURAR);
        if (stockMinimo == null || stockMinimo < 0)
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        Inventario inv = obtenerOCrear(idProducto, idTienda);
        inv.setStockMinimo(stockMinimo);
        return Mapper.inventDTO(repo.save(inv), accessControl.puedeVerCostos());
    }
}
