package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.services.ITiendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// ruta oficial en plural (/api/tiendas) como el resto; /api/tienda se mantiene
// como alias para no romper clientes existentes
@RestController
@RequestMapping({"/api/tiendas", "/api/tienda"})
public class TiendaController {
    @Autowired
    private ITiendaService tiendaService;

    @GetMapping
    public ResponseEntity<List<TiendaDTO>> mostrarTienda() {
        return ResponseEntity.ok(tiendaService.mostrarTiendas());
    }

    // Directorio ligero de sucursales/depositos solo para elegir origen/destino al solicitar transferencias u OC
    @GetMapping("/catalogo")
    public ResponseEntity<List<TiendaDTO>> mostrarCatalogoDestinos() {
        return ResponseEntity.ok(tiendaService.mostrarCatalogoDestinos());
    }

    @PostMapping
    public ResponseEntity<TiendaDTO> create(@Valid @RequestBody TiendaDTO tiendaDTO) {
        TiendaDTO created = tiendaService.crearTienda(tiendaDTO);
        return ResponseEntity.created(URI.create("/api/tiendas/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TiendaDTO> update(@PathVariable Long id, @Valid @RequestBody TiendaDTO tiendaDTO) {
        return ResponseEntity.ok(tiendaService.actualizarTienda(id, tiendaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // antes tenia un error: antes no llamaba al service, devolvia 204 sin borrar nada.
        tiendaService.eliminarTienda(id);
        return ResponseEntity.noContent().build();
    }
}
