package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.dto.MovimientoDTO;
import com.stockcomerce.stockapihb.dto.ResumenStockTiendaDTO;
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
            @RequestParam(required = false) Long idTienda) {
        return ResponseEntity.ok(reporteService.historialMovimientos(idProducto, idTienda));
    }
}
