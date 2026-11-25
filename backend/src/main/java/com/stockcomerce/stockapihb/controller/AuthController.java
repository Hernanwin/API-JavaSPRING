package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.LoginRequestDTO;
import com.stockcomerce.stockapihb.dto.LoginResponseDTO;
import com.stockcomerce.stockapihb.dto.UsuarioDTO;
import com.stockcomerce.stockapihb.services.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me() {
        return ResponseEntity.ok(usuarioService.perfilActual());
    }
}
