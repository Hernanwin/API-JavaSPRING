package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.InventarioDTO;
import com.stockcomerce.stockapihb.dto.MovimientoDTO;
import com.stockcomerce.stockapihb.dto.ResumenStockTiendaDTO;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Entrada;
import com.stockcomerce.stockapihb.models.Inventario;
import com.stockcomerce.stockapihb.models.Salida;
import com.stockcomerce.stockapihb.models.Transferencia;
import com.stockcomerce.stockapihb.repositories.EntradaRepository;
import com.stockcomerce.stockapihb.repositories.InventarioRepository;
import com.stockcomerce.stockapihb.repositories.SalidaRepository;
import com.stockcomerce.stockapihb.repositories.TransferenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService implements IReporteService {

    @Autowired
    private InventarioRepository inventarioRepo;
    @Autowired
    private EntradaRepository entradaRepo;
    @Autowired
    private SalidaRepository salidaRepo;
    @Autowired
    private TransferenciaRepository transferenciaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> stockBajo() {
        return inventarioRepo.findAll().stream()
                .filter(inv -> inv.getStockMinimo() != null
                        && inv.getCantidad() != null
                        && inv.getCantidad() <= inv.getStockMinimo())
                .map(Mapper::inventDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenStockTiendaDTO> resumenPorTienda() {
        List<Inventario> todo = inventarioRepo.findAll();

        return todo.stream()
                .filter(inv -> inv.getTienda() != null)
                .collect(Collectors.groupingBy(inv -> inv.getTienda().getId()))
                .values().stream()
                .map(items -> {
                    var tienda = items.get(0).getTienda();
                    int totalUnidades = items.stream()
                            .mapToInt(i -> i.getCantidad() != null ? i.getCantidad() : 0)
                            .sum();

                    return ResumenStockTiendaDTO.builder()
                            .idTienda(tienda.getId())
                            .nombreTienda(tienda.getNombre())
                            .totalUnidades(totalUnidades)
                            .productosDistintos(items.size())
                            .build();
                })
                .sorted((a, b) -> {
                    if (a.getNombreTienda() == null) return 1;
                    if (b.getNombreTienda() == null) return -1;
                    return a.getNombreTienda().compareTo(b.getNombreTienda());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> historialMovimientos(Long idProducto, Long idTienda) {
        List<MovimientoDTO> movimientos = new ArrayList<>();

        for (Entrada e : entradaRepo.findAll()) {
            Long idProdE = e.getProducto() != null ? e.getProducto().getId() : null;
            Long idTiendaE = e.getTienda() != null ? e.getTienda().getId() : null;

            if (coincide(idProducto, idProdE) && coincide(idTienda, idTiendaE)) {
                movimientos.add(MovimientoDTO.builder()
                        .tipo("ENTRADA")
                        .idProducto(idProdE)
                        .nombreProducto(e.getProducto() != null ? e.getProducto().getNombre() : null)
                        .idTienda(idTiendaE)
                        .nombreTienda(e.getTienda() != null ? e.getTienda().getNombre() : null)
                        .cantidad(e.getCantidad())
                        .fecha(e.getFecha())
                        .build());
            }
        }

        for (Salida s : salidaRepo.findAll()) {
            Long idProdS = s.getProducto() != null ? s.getProducto().getId() : null;
            Long idTiendaS = s.getTienda() != null ? s.getTienda().getId() : null;

            if (coincide(idProducto, idProdS) && coincide(idTienda, idTiendaS)) {
                movimientos.add(MovimientoDTO.builder()
                        .tipo("SALIDA")
                        .idProducto(idProdS)
                        .nombreProducto(s.getProducto() != null ? s.getProducto().getNombre() : null)
                        .idTienda(idTiendaS)
                        .nombreTienda(s.getTienda() != null ? s.getTienda().getNombre() : null)
                        .cantidad(s.getCantidad())
                        .fecha(s.getFecha())
                        .build());
            }
        }

        for (Transferencia t : transferenciaRepo.findAll()) {
            Long idProdT = t.getProducto() != null ? t.getProducto().getId() : null;
            Long idOrigen = t.getTiendaOrigen() != null ? t.getTiendaOrigen().getId() : null;
            Long idDestino = t.getTiendaDestino() != null ? t.getTiendaDestino().getId() : null;

            // una transferencia "toca" tanto la tienda origen como la destino
            boolean tocaTienda = coincide(idTienda, idOrigen) || coincide(idTienda, idDestino);

            if (coincide(idProducto, idProdT) && tocaTienda) {
                movimientos.add(MovimientoDTO.builder()
                        .tipo("TRANSFERENCIA")
                        .idProducto(idProdT)
                        .nombreProducto(t.getProducto() != null ? t.getProducto().getNombre() : null)
                        .idTienda(idOrigen)
                        .nombreTienda(t.getTiendaOrigen() != null ? t.getTiendaOrigen().getNombre() : null)
                        .idTiendaDestino(idDestino)
                        .nombreTiendaDestino(t.getTiendaDestino() != null ? t.getTiendaDestino().getNombre() : null)
                        .cantidad(t.getCantidad())
                        .fecha(t.getFecha())
                        .build());
            }
        }

        // mas reciente primero; los sin fecha (no deberia pasar) van al final
        return movimientos.stream()
                .sorted((m1, m2) -> {
                    if (m1.getFecha() == null && m2.getFecha() == null) return 0;
                    if (m1.getFecha() == null) return 1;
                    if (m2.getFecha() == null) return -1;
                    return m2.getFecha().compareTo(m1.getFecha());
                })
                .toList();
    }

    // filtro opcional: si el parametro pedido es null, no filtra por ese campo
    private boolean coincide(Long filtro, Long valor) {
        return filtro == null || filtro.equals(valor);
    }
}
