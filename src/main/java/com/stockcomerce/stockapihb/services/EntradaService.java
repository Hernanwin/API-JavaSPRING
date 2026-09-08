package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.EntradaDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Entrada;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.repositories.EntradaRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntradaService implements IEntradaService {

    @Autowired
    private EntradaRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private IInventarioService inventarioService;

    @Override
    @Transactional(readOnly = true)
    public List<EntradaDTO> mostrarEntradas() {
        return repo.findAll().stream().map(Mapper::entradaDTO).toList();
    }

    @Transactional
    @Override
    public EntradaDTO crearEntrada(EntradaDTO entradaDTO) {
        if (entradaDTO == null) throw new RuntimeException("EntradaDTO no existe");
        if (entradaDTO.getIdProducto() == null) throw new RuntimeException("Debe indicar el producto");
        if (entradaDTO.getIdTienda() == null) throw new RuntimeException("Debe indicar la tienda");
        if (entradaDTO.getCantidad() == null || entradaDTO.getCantidad() <= 3)
            throw new RuntimeException("La cantidad debe ser mayor a 3");

        Producto producto = productoRepo.findById(entradaDTO.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda tienda = tiendaRepo.findById(entradaDTO.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Tienda no encontrada"));

        // sube el stock en Inventario; si es la primera entrada a esa tienda, crea la fila
        inventarioService.incrementarStock(entradaDTO.getIdProducto(), entradaDTO.getIdTienda(),
                entradaDTO.getCantidad());

        Entrada entrada = Entrada.builder()
                .producto(producto)
                .tienda(tienda)
                .cantidad(entradaDTO.getCantidad())
                .fecha(LocalDateTime.now())
                .observacion(entradaDTO.getObservacion())
                .build();

        return Mapper.entradaDTO(repo.save(entrada));
    }
}
