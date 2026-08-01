package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.services.ITiendaService;
import com.stockcomerce.stockapihb.services.TiendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tienda")
public class TiendaController {
    @Autowired
    private ITiendaService tiendaService;

    @GetMapping
    public ResponseEntity<List<TiendaDTO>> mostrarTienda() {
        return ResponseEntity.ok(tiendaService.mostrarTiendas());
    }

    @PostMapping
    public ResponseEntity<TiendaDTO> create(@RequestBody TiendaDTO tiendaDTO) {
        TiendaDTO created = tiendaService.crearTienda(tiendaDTO);
        return ResponseEntity.created(URI.create("/api/tienda")).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TiendaDTO> update(@PathVariable Long id, @RequestBody TiendaDTO tiendaDTO) {
        return ResponseEntity.ok(tiendaService.actualizarTienda(id, tiendaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TiendaDTO> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}