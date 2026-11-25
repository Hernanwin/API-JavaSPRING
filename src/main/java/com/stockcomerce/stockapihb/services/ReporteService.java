package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.*;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.*;
import com.stockcomerce.stockapihb.repositories.*;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.AlcanceUbicacion;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private AjusteConteoRepository ajusteConteoRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private TiendaRepository tiendaRepo;
    @Autowired
    private VentaRepository ventaRepo;
    @Autowired
    private ApartadoRepository apartadoRepo;
    @Autowired
    private OrdenCompraRepository ordenCompraRepo;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> stockBajo() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.REPORTE_OPERATIVO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return inventarioRepo.findStockBajo().stream()
                .filter(inv -> inv.getTienda() != null && alcance.incluye(inv.getTienda().getId()))
                .map(inv -> Mapper.inventDTO(inv, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenStockTiendaDTO> resumenPorTienda() {
        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.REPORTE_OPERATIVO_VER);
        return inventarioRepo.resumenPorTienda().stream()
                .filter(r -> alcance.incluye((Long) r[0]))
                .map(r -> ResumenStockTiendaDTO.builder()
                        .idTienda((Long) r[0])
                        .nombreTienda((String) r[1])
                        .totalUnidades(((Number) r[2]).intValue())
                        .productosDistintos(((Number) r[3]).intValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> historialMovimientos(Long idProducto, Long idTienda, Integer page, Integer size) {
        if (page != null && page < 0) throw new IllegalArgumentException("page no puede ser negativo");
        if (size != null && size <= 0) throw new IllegalArgumentException("size debe ser mayor a 0");

        AlcanceUbicacion alcance = accessControl.resolverAlcance(Permiso.REPORTE_OPERATIVO_VER);
        if (idTienda != null && !alcance.incluye(idTienda)) {
            accessControl.exigirPermisoEnUbicacion(idTienda, Permiso.REPORTE_OPERATIVO_VER);
        }

        List<MovimientoDTO> movimientos = new ArrayList<>();

        for (Entrada e : entradaRepo.filtrar(idProducto, idTienda)) {
            if (!alcance.incluye(e.getTienda().getId())) continue;
            movimientos.add(MovimientoDTO.builder()
                    .tipo("ENTRADA")
                    .idProducto(e.getProducto().getId())
                    .nombreProducto(e.getProducto().getNombre())
                    .idTienda(e.getTienda().getId())
                    .nombreTienda(e.getTienda().getNombre())
                    .cantidad(e.getCantidad())
                    .fecha(e.getFecha())
                    .build());
        }

        for (Salida s : salidaRepo.filtrar(idProducto, idTienda)) {
            if (!alcance.incluye(s.getTienda().getId())) continue;
            movimientos.add(MovimientoDTO.builder()
                    .tipo("SALIDA")
                    .idProducto(s.getProducto().getId())
                    .nombreProducto(s.getProducto().getNombre())
                    .idTienda(s.getTienda().getId())
                    .nombreTienda(s.getTienda().getNombre())
                    .cantidad(s.getCantidad())
                    .fecha(s.getFecha())
                    .build());
        }

        for (Transferencia t : transferenciaRepo.filtrar(idProducto, idTienda)) {
            if (t.getEstado() == EstadoTransferencia.SOLICITADA
                    || t.getEstado() == EstadoTransferencia.APROBADA
                    || t.getEstado() == EstadoTransferencia.RECHAZADA) {
                continue;
            }
            if (!alcance.incluyeAlguna(t.getTiendaOrigen().getId(), t.getTiendaDestino().getId())) {
                continue;
            }
            movimientos.add(MovimientoDTO.builder()
                    .tipo("TRANSFERENCIA")
                    .idProducto(t.getProducto().getId())
                    .nombreProducto(t.getProducto().getNombre())
                    .idTienda(t.getTiendaOrigen().getId())
                    .nombreTienda(t.getTiendaOrigen().getNombre())
                    .idTiendaDestino(t.getTiendaDestino().getId())
                    .nombreTiendaDestino(t.getTiendaDestino().getNombre())
                    .cantidad(t.getCantidad())
                    .fecha(t.getFecha())
                    .build());
        }

        for (AjusteConteo a : ajusteConteoRepo.filtrar(idProducto, idTienda)) {
            if (!alcance.incluye(a.getTienda().getId())) continue;
            movimientos.add(MovimientoDTO.builder()
                    .tipo("AJUSTE_CONTEO")
                    .idProducto(a.getProducto().getId())
                    .nombreProducto(a.getProducto().getNombre())
                    .idTienda(a.getTienda().getId())
                    .nombreTienda(a.getTienda().getNombre())
                    .cantidad(a.getDiferencia())
                    .fecha(a.getFecha())
                    .build());
        }

        var ordenados = movimientos.stream()
                .sorted(Comparator.comparing(MovimientoDTO::getFecha,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        if (size != null) {
            int p = page != null ? page : 0;
            ordenados = ordenados.skip((long) p * size).limit(size);
        }
        return ordenados.toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMargenProductoDTO> reporteMargenes(Long idProveedor, Long idTienda) {
        accessControl.exigirPermiso(Permiso.REPORTE_ROTACION_MARGENES_VER);
        accessControl.exigirPermiso(Permiso.COSTOS_VER);

        Map<Long, int[]> cantPorProd = new HashMap<>();
        Map<Long, BigDecimal> ingresosPorProd = new HashMap<>();
        Map<Long, BigDecimal> costosPorProd = new HashMap<>();

        for (Venta v : ventaRepo.findAll()) {
            if (idTienda != null && (v.getTienda() == null || !idTienda.equals(v.getTienda().getId()))) {
                continue;
            }
            for (DetalleVenta d : v.getDetalle()) {
                if (d.getProducto() == null) continue;
                Long pid = d.getProducto().getId();
                int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                cantPorProd.computeIfAbsent(pid, k -> new int[]{0})[0] += cant;

                BigDecimal sub = d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO;
                ingresosPorProd.merge(pid, sub, BigDecimal::add);

                BigDecimal costoBase = d.getCostoUnitarioHistorico() != null
                        ? d.getCostoUnitarioHistorico()
                        : (d.getProducto().getCosto() != null ? d.getProducto().getCosto() : BigDecimal.ZERO);
                BigDecimal costoLinea = costoBase.multiply(BigDecimal.valueOf(cant));
                costosPorProd.merge(pid, costoLinea, BigDecimal::add);
            }
        }

        List<ReporteMargenProductoDTO> salida = new ArrayList<>();
        for (Producto p : productoRepo.findAll()) {
            if (idProveedor != null && (p.getProveedor() == null || !idProveedor.equals(p.getProveedor().getId()))) {
                continue;
            }
            BigDecimal precio = p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO;
            BigDecimal costo = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
            BigDecimal margenUnit = precio.subtract(costo);
            BigDecimal pctCat = precio.compareTo(BigDecimal.ZERO) > 0
                    ? margenUnit.multiply(BigDecimal.valueOf(100)).divide(precio, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            int vendidas = cantPorProd.containsKey(p.getId()) ? cantPorProd.get(p.getId())[0] : 0;
            BigDecimal ing = ingresosPorProd.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal cst = costosPorProd.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal utilidad = ing.subtract(cst);
            BigDecimal pctReal = ing.compareTo(BigDecimal.ZERO) > 0
                    ? utilidad.multiply(BigDecimal.valueOf(100)).divide(ing, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            salida.add(ReporteMargenProductoDTO.builder()
                    .idProducto(p.getId())
                    .nombreProducto(p.getNombre())
                    .idProveedor(p.getProveedor() != null ? p.getProveedor().getId() : null)
                    .nombreProveedor(p.getProveedor() != null ? p.getProveedor().getNombre() : null)
                    .precioVentaActual(precio)
                    .costoActual(costo)
                    .margenUnitarioCatalogo(margenUnit)
                    .porcentajeMargenCatalogo(pctCat)
                    .unidadesVendidas(vendidas)
                    .ingresosVentas(ing)
                    .costoVentas(cst)
                    .utilidadBruta(utilidad)
                    .porcentajeMargenRealizado(pctReal)
                    .build());
        }
        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteRotacionDTO> reporteRotacion(Long idTienda, Long idProveedor, Integer dias) {
        accessControl.exigirPermiso(Permiso.REPORTE_ROTACION_MARGENES_VER);
        int ventanaDias = (dias != null && dias > 0) ? dias : 30;
        LocalDateTime desde = LocalDateTime.now().minusDays(ventanaDias);

        Map<String, Integer> salidasPorClave = new HashMap<>();
        for (Salida s : salidaRepo.findAll()) {
            if (s.getProducto() == null || s.getTienda() == null) continue;
            if (s.getFecha() != null && s.getFecha().isBefore(desde)) continue;
            String clave = s.getProducto().getId() + ":" + s.getTienda().getId();
            salidasPorClave.merge(clave, s.getCantidad() != null ? s.getCantidad() : 0, Integer::sum);
        }

        List<ReporteRotacionDTO> resultado = new ArrayList<>();
        for (Inventario inv : inventarioRepo.findAll()) {
            Producto p = inv.getProducto();
            Tienda t = inv.getTienda();
            if (p == null || t == null) continue;
            if (idTienda != null && !idTienda.equals(t.getId())) continue;
            if (idProveedor != null && (p.getProveedor() == null || !idProveedor.equals(p.getProveedor().getId()))) {
                continue;
            }
            int stockActual = inv.getCantidad() != null ? inv.getCantidad() : 0;
            int salidas = salidasPorClave.getOrDefault(p.getId() + ":" + t.getId(), 0);

            // Stock promedio aproximado entre inicio estimado (stockActual + salidas) y stockActual
            double basePromedio = Math.max(1.0, (stockActual + (stockActual + salidas)) / 2.0);
            BigDecimal indice = BigDecimal.valueOf(salidas / basePromedio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal diasInv = salidas > 0
                    ? BigDecimal.valueOf((double) stockActual * ventanaDias / salidas).setScale(1, RoundingMode.HALF_UP)
                    : null;

            resultado.add(ReporteRotacionDTO.builder()
                    .idProducto(p.getId())
                    .nombreProducto(p.getNombre())
                    .idProveedor(p.getProveedor() != null ? p.getProveedor().getId() : null)
                    .nombreProveedor(p.getProveedor() != null ? p.getProveedor().getNombre() : null)
                    .idTienda(t.getId())
                    .nombreTienda(t.getNombre())
                    .stockActual(stockActual)
                    .unidadesSalidasPeriodo(salidas)
                    .indiceRotacion(indice)
                    .diasInventarioEstimados(diasInv)
                    .build());
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteCumplimientoRecepcionDTO> reporteCumplimientoRecepcion(Long idProveedor, Long idTienda) {
        accessControl.exigirPermiso(Permiso.REPORTE_ROTACION_MARGENES_VER);

        List<OrdenCompra> ordenes = ordenCompraRepo.findAll();
        List<Transferencia> transferencias = transferenciaRepo.findAll();

        List<ReporteCumplimientoRecepcionDTO> resultado = new ArrayList<>();
        for (Tienda t : tiendaRepo.findAll()) {
            if (idTienda != null && !idTienda.equals(t.getId())) continue;

            int ocTot = 0, ocComp = 0, ocParc = 0, ocPend = 0, uSol = 0, uRec = 0;
            for (OrdenCompra oc : ordenes) {
                if (!t.getId().equals(oc.getTiendaDestino().getId())) continue;
                if (idProveedor != null && !idProveedor.equals(oc.getProveedor().getId())) continue;
                if (oc.getEstado() == EstadoOrdenCompra.CANCELADA) continue;

                ocTot++;
                if (oc.getEstado() == EstadoOrdenCompra.RECIBIDA_COMPLETA) ocComp++;
                else if (oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL) ocParc++;
                else ocPend++;

                for (DetalleOrdenCompra d : oc.getDetalle()) {
                    uSol += d.getCantidadSolicitada();
                    uRec += d.getCantidadRecibida();
                }
            }

            int trComp = 0, trTran = 0, uEnv = 0, uTrRec = 0;
            for (Transferencia tr : transferencias) {
                if (tr.getTiendaDestino() == null || !t.getId().equals(tr.getTiendaDestino().getId())) continue;
                if (idProveedor != null && (tr.getProducto() == null
                        || tr.getProducto().getProveedor() == null
                        || !idProveedor.equals(tr.getProducto().getProveedor().getId()))) {
                    continue;
                }
                if (tr.getEstado() == EstadoTransferencia.COMPLETADA) {
                    trComp++;
                    uEnv += tr.getCantidadEnviada() != null ? tr.getCantidadEnviada() : tr.getCantidad();
                    uTrRec += tr.getCantidadRecibida() != null ? tr.getCantidadRecibida() : tr.getCantidad();
                } else if (tr.getEstado() == EstadoTransferencia.EN_TRANSITO) {
                    trTran++;
                    uEnv += tr.getCantidadEnviada() != null ? tr.getCantidadEnviada() : tr.getCantidad();
                }
            }

            BigDecimal pctOc = uSol > 0
                    ? BigDecimal.valueOf(uRec * 100.0 / uSol).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal pctTr = uEnv > 0
                    ? BigDecimal.valueOf(uTrRec * 100.0 / uEnv).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            resultado.add(ReporteCumplimientoRecepcionDTO.builder()
                    .idTienda(t.getId())
                    .nombreTienda(t.getNombre())
                    .ordenesCompraTotales(ocTot)
                    .ordenesCompraCompletas(ocComp)
                    .ordenesCompraParciales(ocParc)
                    .ordenesCompraPendientes(ocPend)
                    .unidadesOcSolicitadas(uSol)
                    .unidadesOcRecibidas(uRec)
                    .porcentajeCumplimientoOc(pctOc)
                    .transferenciasCompletadas(trComp)
                    .transferenciasEnTransito(trTran)
                    .unidadesTransfEnviadas(uEnv)
                    .unidadesTransfRecibidas(uTrRec)
                    .porcentajeCumplimientoTransferencias(pctTr)
                    .build());
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteFinancieroConsolidadoDTO reporteFinancieroConsolidado() {
        accessControl.exigirAlcanceGlobal(Permiso.REPORTE_FINANCIERO_CONSOLIDADO_VER);

        int cantVentas = 0;
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal costoVentas = BigDecimal.ZERO;

        for (Venta v : ventaRepo.findAll()) {
            cantVentas++;
            for (DetalleVenta d : v.getDetalle()) {
                BigDecimal sub = d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO;
                ingresos = ingresos.add(sub);
                int c = d.getCantidad() != null ? d.getCantidad() : 0;
                BigDecimal costoUnit = d.getCostoUnitarioHistorico() != null
                        ? d.getCostoUnitarioHistorico()
                        : (d.getProducto() != null && d.getProducto().getCosto() != null
                                ? d.getProducto().getCosto()
                                : BigDecimal.ZERO);
                costoVentas = costoVentas.add(costoUnit.multiply(BigDecimal.valueOf(c)));
            }
        }

        BigDecimal utilidad = ingresos.subtract(costoVentas);
        BigDecimal margenPct = ingresos.compareTo(BigDecimal.ZERO) > 0
                ? utilidad.multiply(BigDecimal.valueOf(100)).divide(ingresos, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal invCosto = BigDecimal.ZERO;
        BigDecimal invVenta = BigDecimal.ZERO;
        for (Inventario inv : inventarioRepo.findAll()) {
            int c = inv.getCantidad() != null ? inv.getCantidad() : 0;
            if (inv.getProducto() != null) {
                BigDecimal costo = inv.getProducto().getCosto() != null ? inv.getProducto().getCosto() : BigDecimal.ZERO;
                BigDecimal precio = inv.getProducto().getPrecio() != null ? inv.getProducto().getPrecio() : BigDecimal.ZERO;
                invCosto = invCosto.add(costo.multiply(BigDecimal.valueOf(c)));
                invVenta = invVenta.add(precio.multiply(BigDecimal.valueOf(c)));
            }
        }

        BigDecimal apartadosTotal = BigDecimal.ZERO;
        BigDecimal apartadosSaldo = BigDecimal.ZERO;
        for (Apartado ap : apartadoRepo.findAll()) {
            if (ap.getEstado() == EstadoApartado.ACTIVO) {
                apartadosTotal = apartadosTotal.add(ap.getTotal() != null ? ap.getTotal() : BigDecimal.ZERO);
                apartadosSaldo = apartadosSaldo.add(ap.getSaldoPendiente() != null ? ap.getSaldoPendiente() : BigDecimal.ZERO);
            }
        }

        BigDecimal ocComprometidas = BigDecimal.ZERO;
        for (OrdenCompra oc : ordenCompraRepo.findAll()) {
            if (oc.getEstado() == EstadoOrdenCompra.APROBADA
                    || oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL) {
                ocComprometidas = ocComprometidas.add(
                        oc.getTotalEstimado() != null ? oc.getTotalEstimado() : BigDecimal.ZERO);
            }
        }

        return ReporteFinancieroConsolidadoDTO.builder()
                .cantidadVentas(cantVentas)
                .ingresosTotalesVentas(ingresos)
                .costoTotalVentas(costoVentas)
                .utilidadBrutaVentas(utilidad)
                .margenBrutoPorcentaje(margenPct)
                .valorInventarioAlCosto(invCosto)
                .valorInventarioAPrecioVenta(invVenta)
                .apartadosActivosTotal(apartadosTotal)
                .apartadosSaldoPendiente(apartadosSaldo)
                .ordenesCompraComprometidas(ocComprometidas)
                .build();
    }
}
