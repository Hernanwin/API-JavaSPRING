package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Inventario;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.repositories.InventarioRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService implements IInventarioService {

    @Autowired
    private InventarioRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarInventario() {
        return repo.findAll().stream().map(Mapper::inventDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarPorTienda(Long idTienda) {
        return repo.findByTiendaId(idTienda).stream().map(Mapper::inventDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> mostrarPorProducto(Long idProducto) {
        return repo.findByProductoId(idProducto).stream().map(Mapper::inventDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioDTO consultarStock(Long idProducto, Long idTienda) {
        // si no hay fila todavia, el stock es 0 (no es un error: nunca entro mercaderia ahi)
        return repo.findByProductoIdAndTiendaId(idProducto, idTienda)
                .map(Mapper::inventDTO)
                .orElse(InventarioDTO.builder()
                        .idProducto(idProducto)
                        .idTienda(idTienda)
                        .cantidad(0)
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

        if (inv.getCantidad() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente: hay " + inv.getCantidad() + ", se pidieron " + cantidad);
        }

        inv.setCantidad(inv.getCantidad() - cantidad);
        return repo.save(inv);
    }

    // busca la fila Producto+Tienda; si nunca existio, la crea en 0 (primer ingreso a esa tienda)
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
                            .stockMinimo(0)
                            .build();
                    return repo.save(nuevo);
                });
    }

    @Override
    @Transactional
    public InventarioDTO actualizarStockMinimo(Long idProducto, Long idTienda, Integer stockMinimo) {
        if (stockMinimo == null || stockMinimo < 0)
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");

        Inventario inv = obtenerOCrear(idProducto, idTienda);
        inv.setStockMinimo(stockMinimo);
        return Mapper.inventDTO(repo.save(inv));
    }
}
