package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.NotificacionDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface INotificacionService {
    // soloNoLeidas = true -> solo pendientes; false/null -> todas (mas reciente primero)
    List<NotificacionDTO> listar(Boolean soloNoLeidas);
    NotificacionDTO marcarLeida(Long id);
    int marcarTodasLeidas();
    // suscripcion en tiempo real (Server-Sent Events) a nuevas alertas
    SseEmitter suscribir();
}
