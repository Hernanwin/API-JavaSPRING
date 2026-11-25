package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.ApartadoDTO;
import com.stockcomerce.stockapihb.models.EstadoApartado;
import com.stockcomerce.stockapihb.services.IApartadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/apartados")
public class ApartadoController {

    @Autowired
    private IApartadoService apartadoService;

    @GetMapping
    public ResponseEntity<List<ApartadoDTO>> listar(
            @RequestParam(required = false) Long idTienda,
            @RequestParam(required = false) EstadoApartado estado) {
        return ResponseEntity.ok(apartadoService.listar(idTienda, estado));
    }

    @PostMapping
    public ResponseEntity<ApartadoDTO> crear(@Valid @RequestBody ApartadoDTO dto) {
        ApartadoDTO creado = apartadoService.crear(dto);
        return ResponseEntity.created(URI.create("/api/apartados/" + creado.getId())).body(creado);
    }

    @PostMapping("/{id}/cobrar")
    public ResponseEntity<ApartadoDTO> cobrar(@PathVariable Long id) {
        return ResponseEntity.ok(apartadoService.cobrar(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApartadoDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(apartadoService.cancelar(id));
    }
}
