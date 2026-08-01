package com.stockcomerce.stockapihb.controller;
import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> mostrarProductos(){
        return ResponseEntity.ok(productoService.mostrarProductos());
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO productoDTO) {
        ProductoDTO creado = productoService.crearProducto(productoDTO);

        return ResponseEntity.created(URI.create("/api/productos/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, productoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}