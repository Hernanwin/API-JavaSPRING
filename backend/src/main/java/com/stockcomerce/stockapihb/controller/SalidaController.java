package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.SalidaDTO;
import com.stockcomerce.stockapihb.services.ISalidaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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

    @PostMapping
    public ResponseEntity<SalidaDTO> crearSalidaManual(@Valid @RequestBody SalidaDTO dto) {
        SalidaDTO creada = salidaService.crearSalidaManual(dto);
        return ResponseEntity.created(URI.create("/api/salidas/" + creada.getId())).body(creada);
    }
}
