package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.OrdenCompraDTO;
import com.stockcomerce.stockapihb.dto.RecepcionOrdenCompraDTO;
import com.stockcomerce.stockapihb.models.EstadoOrdenCompra;

import java.util.List;

public interface IOrdenCompraService {
    List<OrdenCompraDTO> listar(EstadoOrdenCompra estado, Long idProveedor, Long idTiendaDestino);
    OrdenCompraDTO crear(OrdenCompraDTO dto);
    OrdenCompraDTO aprobar(Long id);
    OrdenCompraDTO cancelar(Long id);
    OrdenCompraDTO registrarRecepcion(Long id, RecepcionOrdenCompraDTO recepcion);
}
