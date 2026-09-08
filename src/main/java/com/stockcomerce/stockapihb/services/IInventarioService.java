package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.models.Inventario;

import java.util.List;

public interface IInventarioService {
    // consultas
    List<InventarioDTO> mostrarInventario();
    List<InventarioDTO> mostrarPorTienda(Long idTienda);
    List<InventarioDTO> mostrarPorProducto(Long idProducto);
    InventarioDTO consultarStock(Long idProducto, Long idTienda);

    // usados por Entrada/Salida/Transferencia para mover stock de forma atomica.

    // no se exponen directo en un controller propio: se llaman desde esos servicios.
    Inventario incrementarStock(Long idProducto, Long idTienda, int cantidad);
    Inventario decrementarStock(Long idProducto, Long idTienda, int cantidad);

    // define el umbral de alerta para el reporte de stock bajo (crea la fila si no existe)
    InventarioDTO actualizarStockMinimo(Long idProducto, Long idTienda, Integer stockMinimo);
}
