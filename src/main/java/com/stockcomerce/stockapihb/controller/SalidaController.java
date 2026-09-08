package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.services.ISalidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// solo lectura: las Salidas se generan solas al crear una Venta (fase 4), no via POST directo
@RestController
@RequestMapping("/api/salidas")
public class SalidaController {

    @Autowired
    private ISalidaService salidaService;

    @GetMapping
    public ResponseEntity<List<SalidaDTO>> mostrarSalidas() {
        return ResponseEntity.ok(salidaService.mostrarSalidas());
    }

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<List<SalidaDTO>> porVenta(@PathVariable Long idVenta) {
        return ResponseEntity.ok(salidaService.mostrarPorVenta(idVenta));
    }
}
