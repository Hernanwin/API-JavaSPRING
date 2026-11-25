package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.exception.ConflictException;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.TipoUbicacion;
import com.stockcomerce.stockapihb.repositories.*;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TiendaService implements ITiendaService {

    @Autowired
    private TiendaRepository repo;
    @Autowired
    private InventarioRepository inventarioRepo;
    @Autowired
    private EntradaRepository entradaRepo;
    @Autowired
    private SalidaRepository salidaRepo;
    @Autowired
    private TransferenciaRepository transferenciaRepo;
    @Autowired
    private VentaRepository ventaRepo;
    @Autowired
    private AsignacionRolRepository asignacionRolRepo;
    @Autowired
    private OrdenCompraRepository ordenCompraRepo;
    @Autowired
    private ApartadoRepository apartadoRepo;
    @Autowired
    private AjusteConteoRepository ajusteConteoRepo;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<TiendaDTO> mostrarTiendas() {
        AlcanceUbicacion alcance = accessControl.resolverUbicacionesAsignadas();
        return repo.findAll().stream()
                .filter(t -> alcance.incluye(t.getId()))
                .map(Mapper::tienDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiendaDTO> mostrarCatalogoDestinos() {
        accessControl.exigirPermiso(
                Permiso.TRANSFERENCIA_SOLICITAR,
                Permiso.TRANSFERENCIA_OPERAR,
                Permiso.TRANSFERENCIA_APROBAR,
                Permiso.ORDEN_COMPRA_CREAR,
                Permiso.TIENDA_CONFIGURAR);
        return repo.findAll().stream().map(Mapper::tienDTO).toList();
    }

    @Override
    @Transactional
    public TiendaDTO crearTienda(TiendaDTO tiendaDTO) {
        accessControl.exigirPermiso(Permiso.TIENDA_CONFIGURAR);
        validar(tiendaDTO);
        Tienda tienda = Tienda.builder()
                .nombre(tiendaDTO.getNombre())
                .direccion(tiendaDTO.getDireccion())
                .tipo(tiendaDTO.getTipo() != null ? tiendaDTO.getTipo() : TipoUbicacion.TIENDA)
                .build();
        return Mapper.tienDTO(repo.save(tienda));
    }

    @Override
    @Transactional
    public TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO) {
        accessControl.exigirPermiso(Permiso.TIENDA_CONFIGURAR);
        Tienda tienda = repo.findById(id).orElseThrow(() -> new NotFoundException("Tienda no encontrada"));
        validar(tiendaDTO);
        tienda.setNombre(tiendaDTO.getNombre());
        tienda.setDireccion(tiendaDTO.getDireccion());
        if (tiendaDTO.getTipo() != null) {
            tienda.setTipo(tiendaDTO.getTipo());
        }
        return Mapper.tienDTO(repo.save(tienda));
    }

    @Override
    @Transactional
    public void eliminarTienda(Long id) {
        accessControl.exigirPermiso(Permiso.TIENDA_CONFIGURAR);
        if (!repo.existsById(id))
            throw new NotFoundException("Tienda no encontrada");
        if (inventarioRepo.existsByTiendaId(id) || entradaRepo.existsByTiendaId(id)
                || salidaRepo.existsByTiendaId(id)
                || transferenciaRepo.existsByTiendaOrigenIdOrTiendaDestinoId(id, id)
                || ventaRepo.existsByTiendaId(id)
                || asignacionRolRepo.existsByTiendaId(id)
                || ordenCompraRepo.existsByTiendaDestinoId(id)
                || apartadoRepo.existsByTiendaId(id)
                || ajusteConteoRepo.existsByTiendaId(id)) {
            throw new ConflictException(
                    "No se puede eliminar la tienda: tiene inventario, movimientos, usuarios o ventas asociadas");
        }
        repo.deleteById(id);
    }

    private void validar(TiendaDTO dto) {
        if (dto == null) throw new IllegalArgumentException("TiendaDTO no existe");
        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
    }
}
