package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.dto.MovimientoDTO;
import com.stockcomerce.stockapihb.dto.ResumenStockTiendaDTO;

import java.util.List;

public interface IReporteService {
    // filas de Inventario donde cantidad <= stockMinimo
    List<InventarioDTO> stockBajo();

    // total de unidades y de productos distintos, agrupado por tienda
    List<ResumenStockTiendaDTO> resumenPorTienda();

    // historial combinado de Entrada + Salida + Transferencia, mas reciente primero.
    // idProducto e idTienda son opcionales (null = sin filtrar por ese campo).
    List<MovimientoDTO> historialMovimientos(Long idProducto, Long idTienda);
}
