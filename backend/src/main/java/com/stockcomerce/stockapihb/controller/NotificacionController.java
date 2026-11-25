package com.stockcomerce.stockapihb.controller;

import com.stockcomerce.stockapihb.dto.NotificacionDTO;
import com.stockcomerce.stockapihb.services.INotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

// Alertas de stock bajo: se generan solas cuando el stock de un producto en una
// tienda baja del minimo (12 por defecto). Se pueden consultar o escuchar en vivo.
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private INotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listar(
            @RequestParam(required = false, defaultValue = "false") Boolean soloNoLeidas) {
        return ResponseEntity.ok(notificacionService.listar(soloNoLeidas));
    }

    // tiempo real (Server-Sent Events). Ej. en el navegador:
    // new EventSource('/api/notificaciones/stream').addEventListener('stock-bajo', e => ...)
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return notificacionService.suscribir();
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<NotificacionDTO> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id));
    }

    @PutMapping("/leidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasLeidas() {
        return ResponseEntity.ok(Map.of("actualizadas", notificacionService.marcarTodasLeidas()));
    }
}
