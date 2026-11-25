package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProveedorDTO;

import java.util.List;

public interface IProveedorService {
    List<ProveedorDTO> mostrarProveedores();
    ProveedorDTO crearProveedor(ProveedorDTO dto);
    ProveedorDTO actualizarProveedor(Long id, ProveedorDTO dto);
    void eliminarProveedor(Long id);
}
