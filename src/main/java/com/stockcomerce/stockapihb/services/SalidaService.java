package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Salida;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;
import com.stockcomerce.stockapihb.repositories.SalidaRepository;
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
    private IInventarioService inventarioService;

    @Override
    @Transactional(readOnly = true)
    public List<SalidaDTO> mostrarSalidas() {
        return repo.findAll().stream().map(Mapper::salidaDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaDTO> mostrarPorVenta(Long idVenta) {
        return repo.findByVentaId(idVenta).stream().map(Mapper::salidaDTO).toList();
    }

    @Transactional
    @Override
    public Salida registrarSalidaPorVenta(Venta venta, Producto producto, Tienda tienda, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");

        // baja el stock en Inventario; tira IllegalStateException si no alcanza
        inventarioService.decrementarStock(producto.getId(), tienda.getId(), cantidad);

        Salida salida = Salida.builder()
                .producto(producto)
                .tienda(tienda)
                .venta(venta)
                .cantidad(cantidad)
                .fecha(LocalDateTime.now())
                .build();

        return repo.save(salida);
    }
}
