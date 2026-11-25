package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.exception.ForbiddenException;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.EstadoTransferencia;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Transferencia;
import com.stockcomerce.stockapihb.repositories.InventarioRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.repositories.TransferenciaRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferenciaService implements ITransferenciaService {

    @Autowired
    private TransferenciaRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private InventarioRepository inventarioRepo;
    @Autowired
    private IInventarioService inventarioService;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaDTO> mostrarTransferencias() {
        return listarFiltradas(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaDTO> listarFiltradas(EstadoTransferencia estado) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.TRANSFERENCIA_OPERAR,
                Permiso.TRANSFERENCIA_SOLICITAR,
                Permiso.TRANSFERENCIA_APROBAR,
                Permiso.REPORTE_OPERATIVO_VER);
        return repo.findAll().stream()
                .filter(t -> alcance.incluyeAlguna(
                        t.getTiendaOrigen() != null ? t.getTiendaOrigen().getId() : null,
                        t.getTiendaDestino() != null ? t.getTiendaDestino().getId() : null))
                .filter(t -> estado == null || t.getEstado() == estado)
                .map(Mapper::transferenciaDTO)
                .toList();
    }

    @Transactional
    @Override
    public TransferenciaDTO crearTransferencia(TransferenciaDTO transferenciaDTO) {
        validarDatosBase(transferenciaDTO);

        // Ejecucion directa: exige poder aprobar y operar en ambas puntas (ej. ADMIN)
        accessControl.exigirPermiso(Permiso.TRANSFERENCIA_APROBAR);
        accessControl.exigirPermisoEnUbicacion(transferenciaDTO.getIdTiendaOrigen(), Permiso.TRANSFERENCIA_OPERAR);
        accessControl.exigirPermisoEnUbicacion(transferenciaDTO.getIdTiendaDestino(), Permiso.TRANSFERENCIA_OPERAR);

        Producto producto = productoRepo.findById(transferenciaDTO.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda origen = tiendaRepo.findById(transferenciaDTO.getIdTiendaOrigen())
                .orElseThrow(() -> new NotFoundException("Tienda origen no encontrada"));
        Tienda destino = tiendaRepo.findById(transferenciaDTO.getIdTiendaDestino())
                .orElseThrow(() -> new NotFoundException("Tienda destino no encontrada"));

        boolean destinoTieneInventario = inventarioRepo.existsByProductoIdAndTiendaId(
                transferenciaDTO.getIdProducto(), transferenciaDTO.getIdTiendaDestino());
        if (!destinoTieneInventario) {
            throw new NotFoundException(
                    "La tienda destino nunca registro este producto en inventario; "
                            + "debe recibir una Entrada primero");
        }

        inventarioService.decrementarStock(transferenciaDTO.getIdProducto(),
                transferenciaDTO.getIdTiendaOrigen(), transferenciaDTO.getCantidad());
        inventarioService.incrementarStock(transferenciaDTO.getIdProducto(),
                transferenciaDTO.getIdTiendaDestino(), transferenciaDTO.getCantidad());

        LocalDateTime ahora = LocalDateTime.now();
        String user = accessControl.usernameActual();
        Transferencia transferencia = Transferencia.builder()
                .producto(producto)
                .tiendaOrigen(origen)
                .tiendaDestino(destino)
                .cantidad(transferenciaDTO.getCantidad())
                .cantidadEnviada(transferenciaDTO.getCantidad())
                .cantidadRecibida(transferenciaDTO.getCantidad())
                .estado(EstadoTransferencia.COMPLETADA)
                .fecha(ahora)
                .fechaAprobacion(ahora)
                .fechaEnvio(ahora)
                .fechaRecepcion(ahora)
                .solicitadoPor(user)
                .aprobadoPor(user)
                .enviadoPor(user)
                .recibidoPor(user)
                .observacion(transferenciaDTO.getObservacion())
                .build();

        return Mapper.transferenciaDTO(repo.save(transferencia));
    }

    @Override
    @Transactional
    public TransferenciaDTO solicitarTransferencia(TransferenciaDTO dto) {
        validarDatosBase(dto);
        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.TRANSFERENCIA_SOLICITAR);
        if (!alcance.incluyeAlguna(dto.getIdTiendaOrigen(), dto.getIdTiendaDestino())) {
            throw new ForbiddenException(
                    "Solo puede solicitar transferencias donde el origen o el destino pertenezca a sus ubicaciones asignadas");
        }

        Producto producto = productoRepo.findById(dto.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda origen = tiendaRepo.findById(dto.getIdTiendaOrigen())
                .orElseThrow(() -> new NotFoundException("Tienda origen no encontrada"));
        Tienda destino = tiendaRepo.findById(dto.getIdTiendaDestino())
                .orElseThrow(() -> new NotFoundException("Tienda destino no encontrada"));

        Transferencia t = Transferencia.builder()
                .producto(producto)
                .tiendaOrigen(origen)
                .tiendaDestino(destino)
                .cantidad(dto.getCantidad())
                .estado(EstadoTransferencia.SOLICITADA)
                .fecha(LocalDateTime.now())
                .solicitadoPor(accessControl.usernameActual())
                .observacion(dto.getObservacion())
                .build();

        return Mapper.transferenciaDTO(repo.save(t));
    }

    @Override
    @Transactional
    public TransferenciaDTO aprobarTransferencia(Long id) {
        accessControl.exigirPermiso(Permiso.TRANSFERENCIA_APROBAR);
        Transferencia t = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transferencia no encontrada"));
        if (t.getEstado() != EstadoTransferencia.SOLICITADA) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar transferencias SOLICITADAS (estado actual: " + t.getEstado() + ")");
        }
        t.setEstado(EstadoTransferencia.APROBADA);
        t.setFechaAprobacion(LocalDateTime.now());
        t.setAprobadoPor(accessControl.usernameActual());
        return Mapper.transferenciaDTO(repo.save(t));
    }

    @Override
    @Transactional
    public TransferenciaDTO rechazarTransferencia(Long id, String observacion) {
        accessControl.exigirPermiso(Permiso.TRANSFERENCIA_APROBAR);
        Transferencia t = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transferencia no encontrada"));
        if (t.getEstado() != EstadoTransferencia.SOLICITADA && t.getEstado() != EstadoTransferencia.APROBADA) {
            throw new IllegalStateException(
                    "No se puede rechazar una transferencia en estado " + t.getEstado());
        }
        t.setEstado(EstadoTransferencia.RECHAZADA);
        if (observacion != null && !observacion.isBlank()) {
            t.setObservacion(observacion);
        }
        return Mapper.transferenciaDTO(repo.save(t));
    }

    @Override
    @Transactional
    public TransferenciaDTO enviarTransferencia(Long id) {
        Transferencia t = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transferencia no encontrada"));

        accessControl.exigirPermisoEnUbicacion(t.getTiendaOrigen().getId(), Permiso.TRANSFERENCIA_OPERAR);

        if (t.getEstado() != EstadoTransferencia.APROBADA) {
            throw new IllegalStateException(
                    "La transferencia debe estar APROBADA antes de despacharse (actual: " + t.getEstado() + ")");
        }

        // Descuenta el stock fisico al salir del deposito/tienda origen
        inventarioService.decrementarStock(
                t.getProducto().getId(), t.getTiendaOrigen().getId(), t.getCantidad());

        t.setCantidadEnviada(t.getCantidad());
        t.setEstado(EstadoTransferencia.EN_TRANSITO);
        t.setFechaEnvio(LocalDateTime.now());
        t.setEnviadoPor(accessControl.usernameActual());
        return Mapper.transferenciaDTO(repo.save(t));
    }

    @Override
    @Transactional
    public TransferenciaDTO recibirTransferencia(Long id, Integer cantidadRecibida, String observacion) {
        Transferencia t = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transferencia no encontrada"));

        accessControl.exigirPermisoEnUbicacion(
                t.getTiendaDestino().getId(), Permiso.TRANSFERENCIA_OPERAR, Permiso.ENTRADA_REGISTRAR);

        if (t.getEstado() != EstadoTransferencia.EN_TRANSITO) {
            throw new IllegalStateException(
                    "Solo se pueden recibir transferencias EN_TRANSITO (actual: " + t.getEstado() + ")");
        }

        int enviada = t.getCantidadEnviada() != null ? t.getCantidadEnviada() : t.getCantidad();
        int recibida = cantidadRecibida != null ? cantidadRecibida : enviada;
        if (recibida < 0 || recibida > enviada) {
            throw new IllegalArgumentException(
                    "La cantidad recibida debe estar entre 0 y la cantidad enviada (" + enviada + ")");
        }

        if (recibida > 0) {
            inventarioService.incrementarStock(
                    t.getProducto().getId(), t.getTiendaDestino().getId(), recibida);
        }

        t.setCantidadRecibida(recibida);
        t.setEstado(EstadoTransferencia.COMPLETADA);
        t.setFechaRecepcion(LocalDateTime.now());
        t.setRecibidoPor(accessControl.usernameActual());
        if (observacion != null && !observacion.isBlank()) {
            t.setObservacion(observacion);
        }
        return Mapper.transferenciaDTO(repo.save(t));
    }

    private void validarDatosBase(TransferenciaDTO transferenciaDTO) {
        if (transferenciaDTO == null) throw new IllegalArgumentException("TransferenciaDTO no existe");
        if (transferenciaDTO.getIdProducto() == null) throw new IllegalArgumentException("Debe indicar el producto");
        if (transferenciaDTO.getIdTiendaOrigen() == null || transferenciaDTO.getIdTiendaDestino() == null)
            throw new IllegalArgumentException("Debe indicar tienda origen y tienda destino");
        if (transferenciaDTO.getIdTiendaOrigen().equals(transferenciaDTO.getIdTiendaDestino()))
            throw new IllegalArgumentException("La tienda origen y destino no pueden ser la misma");
        if (transferenciaDTO.getCantidad() == null || transferenciaDTO.getCantidad() <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
    }
}
