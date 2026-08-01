package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TiendaDTO;

import java.util.List;

public interface ITiendaService {
    List<TiendaDTO> mostrarTiendas();
    TiendaDTO crearTienda (TiendaDTO tiendaDTO);
    TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO);
    void eliminarTienda (Long id);
}