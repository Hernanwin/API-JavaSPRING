package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.*;
import com.stockcomerce.stockapihb.services.IReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private IReporteService reporteService;

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> stockBajo() {
        return ResponseEntity.ok(reporteService.stockBajo());
    }

    @GetMapping("/resumen-tiendas")
    public ResponseEntity<List<ResumenStockTiendaDTO>> resumenPorTienda() {
        return ResponseEntity.ok(reporteService.resumenPorTienda());
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDTO>> historialMovimientos(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) Long idTienda,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reporteService.historialMovimientos(idProducto, idTienda, page, size));
    }

    @GetMapping("/margenes")
    public ResponseEntity<List<ReporteMargenProductoDTO>> margenes(
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) Long idTienda) {
        return ResponseEntity.ok(reporteService.reporteMargenes(idProveedor, idTienda));
    }

    @GetMapping("/rotacion")
    public ResponseEntity<List<ReporteRotacionDTO>> rotacion(
            @RequestParam(required = false) Long idTienda,
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(reporteService.reporteRotacion(idTienda, idProveedor, dias));
    }

    @GetMapping("/cumplimiento-recepcion")
    public ResponseEntity<List<ReporteCumplimientoRecepcionDTO>> cumplimientoRecepcion(
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) Long idTienda) {
        return ResponseEntity.ok(reporteService.reporteCumplimientoRecepcion(idProveedor, idTienda));
    }

    @GetMapping("/financiero-consolidado")
    public ResponseEntity<ReporteFinancieroConsolidadoDTO> financieroConsolidado() {
        return ResponseEntity.ok(reporteService.reporteFinancieroConsolidado());
    }
}
