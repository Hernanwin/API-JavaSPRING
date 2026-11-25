package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.OrdenCompraDTO;
import com.stockcomerce.stockapihb.dto.RecepcionOrdenCompraDTO;
import com.stockcomerce.stockapihb.models.EstadoOrdenCompra;
import com.stockcomerce.stockapihb.services.IOrdenCompraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
public class OrdenCompraController {

    @Autowired
    private IOrdenCompraService ordenCompraService;

    @GetMapping
    public ResponseEntity<List<OrdenCompraDTO>> listar(
            @RequestParam(required = false) EstadoOrdenCompra estado,
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) Long idTiendaDestino) {
        return ResponseEntity.ok(ordenCompraService.listar(estado, idProveedor, idTiendaDestino));
    }

    @PostMapping
    public ResponseEntity<OrdenCompraDTO> crear(@Valid @RequestBody OrdenCompraDTO dto) {
        OrdenCompraDTO creada = ordenCompraService.crear(dto);
        return ResponseEntity.created(URI.create("/api/ordenes-compra/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<OrdenCompraDTO> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.aprobar(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<OrdenCompraDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.cancelar(id));
    }

    @PostMapping("/{id}/recepcion")
    public ResponseEntity<OrdenCompraDTO> registrarRecepcion(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid RecepcionOrdenCompraDTO recepcion) {
        return ResponseEntity.ok(ordenCompraService.registrarRecepcion(id, recepcion));
    }
}
