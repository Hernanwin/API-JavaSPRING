package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.AjusteConteoDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.models.AjusteConteo;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.repositories.AjusteConteoRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
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
public class AjusteConteoService implements IAjusteConteoService {

    @Autowired
    private AjusteConteoRepository repo;
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
    public List<AjusteConteoDTO> listar(Long idTienda) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.AJUSTE_CONTEO_EJECUTAR, Permiso.REPORTE_OPERATIVO_VER);
        if (idTienda != null && !alcance.incluye(idTienda)) {
            accessControl.exigirPermisoEnUbicacion(idTienda,
                    Permiso.AJUSTE_CONTEO_EJECUTAR, Permiso.REPORTE_OPERATIVO_VER);
        }
        return repo.findAllByOrderByFechaDescIdDesc().stream()
                .filter(a -> alcance.incluye(a.getTienda().getId()))
                .filter(a -> idTienda == null || a.getTienda().getId().equals(idTienda))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public AjusteConteoDTO registrarAjuste(AjusteConteoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("AjusteConteoDTO obligatorio");
        if (dto.getIdProducto() == null) throw new IllegalArgumentException("Debe indicar el producto");
        if (dto.getIdTienda() == null) throw new IllegalArgumentException("Debe indicar la ubicacion");
        if (dto.getCantidadContada() == null || dto.getCantidadContada() < 0)
            throw new IllegalArgumentException("La cantidad contada no puede ser negativa");

        accessControl.exigirPermisoEnUbicacion(dto.getIdTienda(), Permiso.AJUSTE_CONTEO_EJECUTAR);

        Producto producto = productoRepo.findById(dto.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda tienda = tiendaRepo.findById(dto.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Ubicacion no encontrada"));

        int anterior = inventarioService.consultarStock(producto.getId(), tienda.getId()).getCantidad();
        inventarioService.fijarStockPorConteo(producto.getId(), tienda.getId(), dto.getCantidadContada());

        AjusteConteo ajuste = AjusteConteo.builder()
                .producto(producto)
                .tienda(tienda)
                .cantidadSistemaAnterior(anterior)
                .cantidadContada(dto.getCantidadContada())
                .diferencia(dto.getCantidadContada() - anterior)
                .motivo(dto.getMotivo() != null && !dto.getMotivo().isBlank() ? dto.getMotivo() : "CONTEO_FISICO")
                .ejecutadoPor(accessControl.usernameActual())
                .fecha(LocalDateTime.now())
                .build();

        return toDTO(repo.save(ajuste));
    }

    private AjusteConteoDTO toDTO(AjusteConteo a) {
        return AjusteConteoDTO.builder()
                .id(a.getId())
                .idProducto(a.getProducto().getId())
                .nombreProducto(a.getProducto().getNombre())
                .idTienda(a.getTienda().getId())
                .nombreTienda(a.getTienda().getNombre())
                .cantidadSistemaAnterior(a.getCantidadSistemaAnterior())
                .cantidadContada(a.getCantidadContada())
                .diferencia(a.getDiferencia())
                .motivo(a.getMotivo())
                .ejecutadoPor(a.getEjecutadoPor())
                .fecha(a.getFecha())
                .build();
    }
}
