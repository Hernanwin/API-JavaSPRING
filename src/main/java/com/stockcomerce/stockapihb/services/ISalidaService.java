package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Salida;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;

import java.util.List;

public interface ISalidaService {
    List<SalidaDTO> mostrarSalidas();
    List<SalidaDTO> mostrarPorVenta(Long idVenta);

    // se llama desde VentaService al crear una venta (fase 4); no hay POST publico
    // porque, por ahora, el unico motivo de Salida es VENTA.
    Salida registrarSalidaPorVenta(Venta venta, Producto producto, Tienda tienda, int cantidad);
}
