package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.models.EstadoTransferencia;
import com.stockcomerce.stockapihb.services.ITransferenciaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<TransferenciaDTO>> mostrarTransferencias(
            @RequestParam(required = false) EstadoTransferencia estado) {
        return ResponseEntity.ok(transferenciaService.listarFiltradas(estado));
    }

    @PostMapping
    public ResponseEntity<TransferenciaDTO> crear(@Valid @RequestBody TransferenciaDTO transferenciaDTO) {
        TransferenciaDTO creada = transferenciaService.crearTransferencia(transferenciaDTO);
        return ResponseEntity.created(URI.create("/api/transferencias/" + creada.getId())).body(creada);
    }

    @PostMapping("/solicitar")
    public ResponseEntity<TransferenciaDTO> solicitar(@Valid @RequestBody TransferenciaDTO transferenciaDTO) {
        TransferenciaDTO creada = transferenciaService.solicitarTransferencia(transferenciaDTO);
        return ResponseEntity.created(URI.create("/api/transferencias/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<TransferenciaDTO> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(transferenciaService.aprobarTransferencia(id));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<TransferenciaDTO> rechazar(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion) {
        return ResponseEntity.ok(transferenciaService.rechazarTransferencia(id, observacion));
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<TransferenciaDTO> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(transferenciaService.enviarTransferencia(id));
    }

    @PutMapping("/{id}/recibir")
    public ResponseEntity<TransferenciaDTO> recibir(
            @PathVariable Long id,
            @RequestParam(required = false) Integer cantidadRecibida,
            @RequestParam(required = false) String observacion) {
        return ResponseEntity.ok(transferenciaService.recibirTransferencia(id, cantidadRecibida, observacion));
    }
}
