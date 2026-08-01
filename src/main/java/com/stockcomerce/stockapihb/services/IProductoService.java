package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.dto.TiendaDTO;

import java.util.List;

public interface IProductoService {
    List<ProductoDTO> mostrarProductos();
    ProductoDTO crearProducto (ProductoDTO productoDTO);
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO);
    void eliminarProducto (Long id);
}