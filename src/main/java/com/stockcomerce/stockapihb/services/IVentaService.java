package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;

import java.util.List;

public interface IVentaService {
    List<VentaDTO> mostrarVentas();
    VentaDTO crearVenta (VentaDTO ventaDTO);
    VentaDTO actualizarVenta(Long id, VentaDTO ventaDTO);
    void eliminarVenta (Long id);
}