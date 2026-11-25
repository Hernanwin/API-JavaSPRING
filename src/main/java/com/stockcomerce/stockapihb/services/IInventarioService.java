package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioConsolidadoDTO;
import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.models.Inventario;

import java.util.List;

public interface IInventarioService {
    // consultas filtradas automaticamente por alcance de ubicacion y permisos de costos
    List<InventarioDTO> mostrarInventario();
    List<InventarioConsolidadoDTO> mostrarConsolidado(Long idProveedor);
    List<InventarioDTO> mostrarPorProveedor(Long idProveedor);
    List<InventarioDTO> mostrarPorTienda(Long idTienda);
    List<InventarioDTO> mostrarPorProducto(Long idProducto);
    InventarioDTO consultarStock(Long idProducto, Long idTienda);

    // movimientos atomicos internos desde Entrada/Salida/Transferencia/Conteo/Apartado
    Inventario incrementarStock(Long idProducto, Long idTienda, int cantidad);
    Inventario decrementarStock(Long idProducto, Long idTienda, int cantidad);
    Inventario fijarStockPorConteo(Long idProducto, Long idTienda, int cantidadContada);
    Inventario reservarStock(Long idProducto, Long idTienda, int cantidad);
    Inventario liberarReserva(Long idProducto, Long idTienda, int cantidad);

    InventarioDTO actualizarStockMinimo(Long idProducto, Long idTienda, Integer stockMinimo);
}
