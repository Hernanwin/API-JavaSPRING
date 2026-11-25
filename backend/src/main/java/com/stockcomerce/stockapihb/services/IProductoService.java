package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProductoDTO;

import java.util.List;

public interface IProductoService {
    List<ProductoDTO> mostrarProductos();
    List<ProductoDTO> mostrarProductosPorProveedor(Long idProveedor);
    ProductoDTO crearProducto(ProductoDTO productoDTO);
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO);
    void eliminarProducto(Long id);
}
