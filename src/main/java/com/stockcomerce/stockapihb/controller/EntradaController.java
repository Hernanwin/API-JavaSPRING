package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.EntradaDTO;
import com.stockcomerce.stockapihb.services.IEntradaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    @Autowired
    private IEntradaService entradaService;

    @GetMapping
    public ResponseEntity<List<EntradaDTO>> mostrarEntradas() {
        return ResponseEntity.ok(entradaService.mostrarEntradas());
    }

    @PostMapping
    public ResponseEntity<EntradaDTO> crear(@RequestBody EntradaDTO entradaDTO) {
        EntradaDTO creado = entradaService.crearEntrada(entradaDTO);
        return ResponseEntity.created(URI.create("/api/entradas/" + creado.getId())).body(creado);
    }
}

