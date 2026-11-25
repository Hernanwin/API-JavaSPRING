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

    // Salida manual/operativa de deposito (merma, rotura, despacho, consumo interno)
    SalidaDTO crearSalidaManual(SalidaDTO dto);

    // Salida automatica originada al cobrar una Venta en POS
    Salida registrarSalidaPorVenta(Venta venta, Producto producto, Tienda tienda, int cantidad);

    void revertirSalidasDeVenta(Venta venta);
    void reasignarTiendaDeVenta(Venta venta, Tienda nuevaTienda);
}
