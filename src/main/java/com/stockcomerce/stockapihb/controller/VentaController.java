package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.services.IVentaService;
import com.stockcomerce.stockapihb.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    private IVentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaDTO>> mostrarVentas() {
        return ResponseEntity.ok(ventaService.mostrarVentas());
    }

    @PostMapping
    public ResponseEntity<VentaDTO> create(@RequestBody VentaDTO ventaDTO) {
        VentaDTO created = ventaService.crearVenta(ventaDTO);
        return ResponseEntity.created(URI.create("/api/ventas/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentaDTO> update(@PathVariable Long id, @RequestBody VentaDTO ventaDTO) {
        return ResponseEntity.ok(ventaService.actualizarVenta(id, ventaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<VentaDTO> delete(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }
}