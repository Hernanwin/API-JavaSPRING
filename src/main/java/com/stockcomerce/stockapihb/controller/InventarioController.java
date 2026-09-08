package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.services.IInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo lectura: el stock no se edita a mano aca, se mueve por /api/entradas, /api/salidas, /api/transferencias
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private IInventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<InventarioDTO>> mostrarInventario() {
        return ResponseEntity.ok(inventarioService.mostrarInventario());
    }

    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<List<InventarioDTO>> porTienda(@PathVariable Long idTienda) {
        return ResponseEntity.ok(inventarioService.mostrarPorTienda(idTienda));
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<InventarioDTO>> porProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(inventarioService.mostrarPorProducto(idProducto));
    }

    @GetMapping("/stock")
    public ResponseEntity<InventarioDTO> consultarStock(@RequestParam Long idProducto,
                                                        @RequestParam Long idTienda) {
        return ResponseEntity.ok(inventarioService.consultarStock(idProducto, idTienda));
    }

    @PutMapping("/stock-minimo")
    public ResponseEntity<InventarioDTO> actualizarStockMinimo(@RequestParam Long idProducto,
                                                               @RequestParam Long idTienda,
                                                               @RequestParam Integer stockMinimo) {
        return ResponseEntity.ok(inventarioService.actualizarStockMinimo(idProducto, idTienda, stockMinimo));
    }
}

