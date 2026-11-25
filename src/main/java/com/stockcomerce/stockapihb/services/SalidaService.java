package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Salida;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.SalidaRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalidaService implements ISalidaService {

    @Autowired
    private SalidaRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private IInventarioService inventarioService;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<SalidaDTO> mostrarSalidas() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.SALIDA_MANUAL_REGISTRAR, Permiso.POS_COBRAR, Permiso.REPORTE_OPERATIVO_VER);
        return repo.findAll().stream()
                .filter(s -> s.getTienda() != null && alcance.incluye(s.getTienda().getId()))
                .map(Mapper::salidaDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaDTO> mostrarPorVenta(Long idVenta) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.SALIDA_MANUAL_REGISTRAR, Permiso.POS_COBRAR, Permiso.REPORTE_OPERATIVO_VER, Permiso.VENTA_ADMINISTRAR);
        return repo.findByVentaId(idVenta).stream()
                .filter(s -> s.getTienda() != null && alcance.incluye(s.getTienda().getId()))
                .map(Mapper::salidaDTO)
                .toList();
    }

    @Override
    @Transactional
    public SalidaDTO crearSalidaManual(SalidaDTO dto) {
        if (dto == null) throw new IllegalArgumentException("SalidaDTO no existe");
        if (dto.getIdProducto() == null) throw new IllegalArgumentException("Debe indicar el producto");
        if (dto.getIdTienda() == null) throw new IllegalArgumentException("Debe indicar la ubicacion");
        if (dto.getCantidad() == null || dto.getCantidad() <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");

        accessControl.exigirPermisoEnUbicacion(dto.getIdTienda(), Permiso.SALIDA_MANUAL_REGISTRAR);

        Producto producto = productoRepo.findById(dto.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda tienda = tiendaRepo.findById(dto.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Ubicacion no encontrada"));

        inventarioService.decrementarStock(producto.getId(), tienda.getId(), dto.getCantidad());

        Salida salida = Salida.builder()
                .producto(producto)
                .tienda(tienda)
                .cantidad(dto.getCantidad())
                .fecha(LocalDateTime.now())
                .motivo(dto.getMotivo() != null && !dto.getMotivo().isBlank() ? dto.getMotivo() : "SALIDA_OPERATIVA")
                .observacion(dto.getObservacion())
                .registradoPor(accessControl.usernameActual())
                .build();

        return Mapper.salidaDTO(repo.save(salida));
    }

    @Transactional
    @Override
    public Salida registrarSalidaPorVenta(Venta venta, Producto producto, Tienda tienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");

        inventarioService.decrementarStock(producto.getId(), tienda.getId(), cantidad);

        Salida salida = Salida.builder()
                .producto(producto)
                .tienda(tienda)
                .venta(venta)
                .cantidad(cantidad)
                .fecha(LocalDateTime.now())
                .motivo("VENTA")
                .registradoPor(accessControl.usernameActual())
                .build();

        return repo.save(salida);
    }

    @Transactional
    @Override
    public void revertirSalidasDeVenta(Venta venta) {
        List<Salida> salidas = repo.findByVentaId(venta.getId());
        for (Salida s : salidas) {
            inventarioService.incrementarStock(s.getProducto().getId(), s.getTienda().getId(), s.getCantidad());
        }
        repo.deleteAll(salidas);
    }

    @Transactional
    @Override
    public void reasignarTiendaDeVenta(Venta venta, Tienda nuevaTienda) {
        for (Salida s : repo.findByVentaId(venta.getId())) {
            Long idProducto = s.getProducto().getId();
            inventarioService.decrementarStock(idProducto, nuevaTienda.getId(), s.getCantidad());
            inventarioService.incrementarStock(idProducto, s.getTienda().getId(), s.getCantidad());
            s.setTienda(nuevaTienda);
            repo.save(s);
        }
    }
}
