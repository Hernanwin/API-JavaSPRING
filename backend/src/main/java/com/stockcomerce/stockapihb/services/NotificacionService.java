package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.NotificacionDTO;
import com.stockcomerce.stockapihb.events.StockBajoEvent;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Notificacion;
import com.stockcomerce.stockapihb.repositories.NotificacionRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificacionService implements INotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    @Autowired
    private NotificacionRepository repo;
    @Autowired
    private AccessControlService accessControl;

    private record SuscriptorAlcance(SseEmitter emitter, AlcanceUbicacion alcance) {}

    private final List<SuscriptorAlcance> suscriptores = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStockBajo(StockBajoEvent e) {
        String mensaje = String.format(
                "Stock bajo: '%s' en la tienda '%s' quedo en %d unidades (minimo %d)",
                e.nombreProducto(), e.nombreTienda(), e.cantidadActual(), e.stockMinimo());

        Notificacion n = repo.save(Notificacion.builder()
                .tipo("STOCK_BAJO")
                .idProducto(e.idProducto())
                .nombreProducto(e.nombreProducto())
                .idTienda(e.idTienda())
                .nombreTienda(e.nombreTienda())
                .cantidadActual(e.cantidadActual())
                .stockMinimo(e.stockMinimo())
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .build());

        log.warn(mensaje);
        emitir(Mapper.notifDTO(n));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> listar(Boolean soloNoLeidas) {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.INVENTARIO_VER, Permiso.REPORTE_OPERATIVO_VER);
        List<Notificacion> lista = Boolean.TRUE.equals(soloNoLeidas)
                ? repo.findByLeidaFalseOrderByFechaDescIdDesc()
                : repo.findAllByOrderByFechaDescIdDesc();
        return lista.stream()
                .filter(n -> alcance.incluye(n.getIdTienda()))
                .map(Mapper::notifDTO)
                .toList();
    }

    @Override
    @Transactional
    public NotificacionDTO marcarLeida(Long id) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Notificacion no encontrada"));
        accessControl.exigirPermisoEnUbicacion(
                n.getIdTienda(), Permiso.INVENTARIO_VER, Permiso.REPORTE_OPERATIVO_VER);
        n.setLeida(true);
        return Mapper.notifDTO(repo.save(n));
    }

    @Override
    @Transactional
    public int marcarTodasLeidas() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.INVENTARIO_VER, Permiso.REPORTE_OPERATIVO_VER);
        if (alcance.global()) {
            return repo.marcarTodasLeidas();
        }
        int cont = 0;
        for (Notificacion n : repo.findByLeidaFalseOrderByFechaDescIdDesc()) {
            if (alcance.incluye(n.getIdTienda())) {
                n.setLeida(true);
                repo.save(n);
                cont++;
            }
        }
        return cont;
    }

    @Override
    public SseEmitter suscribir() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(
                Permiso.INVENTARIO_VER, Permiso.REPORTE_OPERATIVO_VER);
        SseEmitter emitter = new SseEmitter(0L);
        SuscriptorAlcance sub = new SuscriptorAlcance(emitter, alcance);
        suscriptores.add(sub);
        emitter.onCompletion(() -> suscriptores.remove(sub));
        emitter.onTimeout(() -> suscriptores.remove(sub));
        emitter.onError(ex -> suscriptores.remove(sub));
        return emitter;
    }

    private void emitir(NotificacionDTO dto) {
        for (SuscriptorAlcance sub : suscriptores) {
            if (!sub.alcance().incluye(dto.getIdTienda())) {
                continue;
            }
            try {
                sub.emitter().send(SseEmitter.event().name("stock-bajo").data(dto));
            } catch (Exception ex) {
                suscriptores.remove(sub);
            }
        }
    }
}
