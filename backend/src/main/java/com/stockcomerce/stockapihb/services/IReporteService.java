package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.*;

import java.util.List;

public interface IReporteService {
    List<InventarioDTO> stockBajo();
    List<ResumenStockTiendaDTO> resumenPorTienda();
    List<MovimientoDTO> historialMovimientos(Long idProducto, Long idTienda, Integer page, Integer size);

    // Supervisor de compras / Admin
    List<ReporteMargenProductoDTO> reporteMargenes(Long idProveedor, Long idTienda);
    List<ReporteRotacionDTO> reporteRotacion(Long idTienda, Long idProveedor, Integer dias);
    List<ReporteCumplimientoRecepcionDTO> reporteCumplimientoRecepcion(Long idProveedor, Long idTienda);

    // Restringido estrictamente a direccion/administracion financiera (Permiso.REPORTE_FINANCIERO_CONSOLIDADO_VER)
    ReporteFinancieroConsolidadoDTO reporteFinancieroConsolidado();
}
