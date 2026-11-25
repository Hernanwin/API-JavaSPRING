package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.AjusteConteoDTO;
import com.stockcomerce.stockapihb.services.IAjusteConteoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ajustes-conteo")
public class AjusteConteoController {

    @Autowired
    private IAjusteConteoService ajusteService;

    @GetMapping
    public ResponseEntity<List<AjusteConteoDTO>> listar(@RequestParam(required = false) Long idTienda) {
        return ResponseEntity.ok(ajusteService.listar(idTienda));
    }

    @PostMapping
    public ResponseEntity<AjusteConteoDTO> registrar(@Valid @RequestBody AjusteConteoDTO dto) {
        AjusteConteoDTO creado = ajusteService.registrarAjuste(dto);
        return ResponseEntity.created(URI.create("/api/ajustes-conteo/" + creado.getId())).body(creado);
    }
}
