package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Transferencia;
import com.stockcomerce.stockapihb.repositories.InventarioRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.repositories.TransferenciaRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaDTO> mostrarTransferencias() {
        return repo.findAll().stream().map(Mapper::transferenciaDTO).toList();
    }

    @Transactional
    @Override
    public TransferenciaDTO crearTransferencia(TransferenciaDTO transferenciaDTO) {
        if (transferenciaDTO == null) throw new RuntimeException("TransferenciaDTO no existe");
        if (transferenciaDTO.getIdProducto() == null) throw new RuntimeException("Debe indicar el producto");
        if (transferenciaDTO.getIdTiendaOrigen() == null || transferenciaDTO.getIdTiendaDestino() == null)
            throw new RuntimeException("Debe indicar tienda origen y tienda destino");
        if (transferenciaDTO.getIdTiendaOrigen().equals(transferenciaDTO.getIdTiendaDestino()))
            throw new RuntimeException("La tienda origen y destino no pueden ser la misma");
        if (transferenciaDTO.getCantidad() == null || transferenciaDTO.getCantidad() <= 0)
            throw new RuntimeException("La cantidad debe ser mayor a 0");

        Producto producto = productoRepo.findById(transferenciaDTO.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda origen = tiendaRepo.findById(transferenciaDTO.getIdTiendaOrigen())
                .orElseThrow(() -> new NotFoundException("Tienda origen no encontrada"));
        Tienda destino = tiendaRepo.findById(transferenciaDTO.getIdTiendaDestino())
                .orElseThrow(() -> new NotFoundException("Tienda destino no encontrada"));

        // regla elegida: la tienda destino debe tener YA una fila de inventario para este
        // producto (aunque sea en 0). Si nunca lo tuvo, se rechaza la transferencia.
        boolean destinoTieneInventario = inventarioRepo.existsByProductoIdAndTiendaId(
                transferenciaDTO.getIdProducto(), transferenciaDTO.getIdTiendaDestino());
        if (!destinoTieneInventario) {
            throw new NotFoundException(
                    "La tienda destino nunca registro este producto en inventario; "
                            + "debe recibir una Entrada primero");
        }

        // baja en origen (valida stock suficiente) y sube en destino, atomico dentro de la misma transaccion
        inventarioService.decrementarStock(transferenciaDTO.getIdProducto(),
                transferenciaDTO.getIdTiendaOrigen(), transferenciaDTO.getCantidad());
        inventarioService.incrementarStock(transferenciaDTO.getIdProducto(),
                transferenciaDTO.getIdTiendaDestino(), transferenciaDTO.getCantidad());

        Transferencia transferencia = Transferencia.builder()
                .producto(producto)
                .tiendaOrigen(origen)
                .tiendaDestino(destino)
                .cantidad(transferenciaDTO.getCantidad())
                .fecha(LocalDateTime.now())
                .build();

        return Mapper.transferenciaDTO(repo.save(transferencia));
    }
}
