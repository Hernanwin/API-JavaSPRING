package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.services.ITransferenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transferencias")

public class TransferenciaController {

    @Autowired
    private ITransferenciaService transferenciaService;

    @GetMapping
    public ResponseEntity<List<TransferenciaDTO>> mostrarTransferencias() {
        return ResponseEntity.ok(transferenciaService.mostrarTransferencias());
    }

    @PostMapping
    public ResponseEntity<TransferenciaDTO> crear(@RequestBody TransferenciaDTO transferenciaDTO) {
        TransferenciaDTO creada = transferenciaService.crearTransferencia(transferenciaDTO);
        return ResponseEntity.created(URI.create("/api/transferencias/" + creada.getId())).body(creada);
    }
}

