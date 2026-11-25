package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.AsignacionRolDTO;
import com.stockcomerce.stockapihb.dto.CrearUsuarioDTO;
import com.stockcomerce.stockapihb.dto.RolDTO;
import com.stockcomerce.stockapihb.dto.UsuarioDTO;
import com.stockcomerce.stockapihb.services.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("/api/roles")
    public ResponseEntity<List<RolDTO>> listarRoles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    @GetMapping("/api/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping("/api/usuarios")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody CrearUsuarioDTO dto) {
        UsuarioDTO creado = usuarioService.crearUsuario(dto);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.getId())).body(creado);
    }

    @PutMapping("/api/usuarios/{id}/asignaciones")
    public ResponseEntity<UsuarioDTO> actualizarAsignaciones(
            @PathVariable Long id,
            @RequestBody List<@Valid AsignacionRolDTO> asignaciones) {
        return ResponseEntity.ok(usuarioService.actualizarAsignaciones(id, asignaciones));
    }

    @PutMapping("/api/usuarios/{id}/estado")
    public ResponseEntity<UsuarioDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }
}
