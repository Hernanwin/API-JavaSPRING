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
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
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
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<EntradaDTO> mostrarEntradas() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.ENTRADA_REGISTRAR, Permiso.REPORTE_OPERATIVO_VER);
        return repo.findAll().stream()
                .filter(e -> e.getTienda() != null && alcance.incluye(e.getTienda().getId()))
                .map(Mapper::entradaDTO)
                .toList();
    }

    @Transactional
    @Override
    public EntradaDTO crearEntrada(EntradaDTO entradaDTO) {
        if (entradaDTO == null) throw new IllegalArgumentException("EntradaDTO no existe");
        if (entradaDTO.getIdProducto() == null) throw new IllegalArgumentException("Debe indicar el producto");
        if (entradaDTO.getIdTienda() == null) throw new IllegalArgumentException("Debe indicar la tienda");
        if (entradaDTO.getCantidad() == null || entradaDTO.getCantidad() <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");

        accessControl.exigirPermisoEnUbicacion(entradaDTO.getIdTienda(), Permiso.ENTRADA_REGISTRAR);

        Producto producto = productoRepo.findById(entradaDTO.getIdProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Tienda tienda = tiendaRepo.findById(entradaDTO.getIdTienda())
                .orElseThrow(() -> new NotFoundException("Tienda no encontrada"));

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
