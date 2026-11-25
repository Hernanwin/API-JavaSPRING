package com.stockcomerce.stockapihb;

import com.stockcomerce.stockapihb.dto.*;
import com.stockcomerce.stockapihb.exception.ConflictException;
import com.stockcomerce.stockapihb.models.Notificacion;
import com.stockcomerce.stockapihb.repositories.NotificacionRepository;
import com.stockcomerce.stockapihb.repositories.VentaRepository;
import com.stockcomerce.stockapihb.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Tests de integracion sobre H2 en memoria. NO son @Transactional a proposito:
// las notificaciones se generan AFTER_COMMIT, asi que cada llamada a un service
// tiene que confirmar su propia transaccion. Cada test crea su producto/tienda.
@SpringBootTest
class StockFlowIntegrationTest {

    @Autowired IProductoService productoService;
    @Autowired ITiendaService tiendaService;
    @Autowired IEntradaService entradaService;
    @Autowired IVentaService ventaService;
    @Autowired ITransferenciaService transferenciaService;
    @Autowired IInventarioService inventarioService;
    @Autowired ISalidaService salidaService;
    @Autowired IReporteService reporteService;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired VentaRepository ventaRepo;

    // ---------- helpers ----------

    private ProductoDTO producto(String precio) {
        return productoService.crearProducto(ProductoDTO.builder()
                .nombre("Prod-" + UUID.randomUUID()).precio(new BigDecimal(precio)).categoria("test").build());
    }

    private TiendaDTO tienda() {
        return tiendaService.crearTienda(TiendaDTO.builder()
                .nombre("Tienda-" + UUID.randomUUID()).direccion("calle").build());
    }

    private void entrada(Long idProducto, Long idTienda, int cantidad) {
        entradaService.crearEntrada(EntradaDTO.builder()
                .idProducto(idProducto).idTienda(idTienda).cantidad(cantidad).build());
    }

    private VentaDTO vender(Long idProducto, Long idTienda, int cantidad) {
        return ventaService.crearVenta(VentaDTO.builder()
                .idTienda(idTienda)
                .detalle(List.of(DetalleVentaDTO.builder().idProducto(idProducto).cantidadProd(cantidad).build()))
                .build());
    }

    private int stock(Long idProducto, Long idTienda) {
        return inventarioService.consultarStock(idProducto, idTienda).getCantidad();
    }

    private List<Notificacion> notificaciones(Long idProducto) {
        return notificacionRepo.findByIdProductoOrderByIdAsc(idProducto);
    }

    // ---------- tests ----------

    @Test
    void entradaAceptaCantidadPequenaYCreaInventarioConMinimo12() {
        var p = producto("10.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 1);

        InventarioDTO inv = inventarioService.consultarStock(p.getId(), t.getId());
        assertEquals(1, inv.getCantidad());
        assertEquals(12, inv.getStockMinimo());
    }

    @Test
    void ventaDescuentaStockRegistraSalidasYUsaPrecioDelProducto() {
        var p = producto("2.50");
        var t = tienda();
        entrada(p.getId(), t.getId(), 30);

        VentaDTO v = ventaService.crearVenta(VentaDTO.builder()
                .idTienda(t.getId())
                .detalle(List.of(DetalleVentaDTO.builder()
                        .idProducto(p.getId()).cantidadProd(4)
                        .precioUnitario(new BigDecimal("0.01")) // debe ignorarse
                        .build()))
                .build());

        assertEquals(26, stock(p.getId(), t.getId()));
        assertEquals(0, new BigDecimal("10.00").compareTo(v.getTotal()));
        assertEquals(0, new BigDecimal("2.50").compareTo(v.getDetalle().get(0).getPrecioUnitario()));
        assertEquals("COMPLETADA", v.getEstado());
        assertNotNull(v.getFechaHora());
        assertEquals(1, salidaService.mostrarPorVenta(v.getId()).size());
    }

    @Test
    void ventaSinStockSuficienteHaceRollbackCompleto() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 5);
        long ventasAntes = ventaRepo.count();

        assertThrows(IllegalStateException.class, () -> vender(p.getId(), t.getId(), 6));

        assertEquals(5, stock(p.getId(), t.getId()));
        assertEquals(ventasAntes, ventaRepo.count());
    }

    @Test
    void notificaSoloAlCruzarElMinimoDe12HaciaAbajo() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 15);

        vender(p.getId(), t.getId(), 3);                 // 15 -> 12: todavia no
        assertTrue(notificaciones(p.getId()).isEmpty());

        vender(p.getId(), t.getId(), 1);                 // 12 -> 11: cruza -> notifica
        List<Notificacion> n = notificaciones(p.getId());
        assertEquals(1, n.size());
        assertEquals(11, n.get(0).getCantidadActual());
        assertEquals(12, n.get(0).getStockMinimo());
        assertEquals(t.getId(), n.get(0).getIdTienda());
        assertFalse(n.get(0).isLeida());

        vender(p.getId(), t.getId(), 1);                 // 11 -> 10: ya estaba abajo, no repite
        assertEquals(1, notificaciones(p.getId()).size());

        entrada(p.getId(), t.getId(), 10);               // repone a 20
        vender(p.getId(), t.getId(), 10);                // 20 -> 10: cruza de nuevo
        assertEquals(2, notificaciones(p.getId()).size());
    }

    @Test
    void ventaFallidaNoGeneraNotificacion() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 12);

        // 2 renglones: el primero bajaria a 2 (cruza), el segundo no tiene stock -> rollback
        assertThrows(IllegalStateException.class, () -> ventaService.crearVenta(VentaDTO.builder()
                .idTienda(t.getId())
                .detalle(List.of(
                        DetalleVentaDTO.builder().idProducto(p.getId()).cantidadProd(10).build(),
                        DetalleVentaDTO.builder().idProducto(p.getId()).cantidadProd(5).build()))
                .build()));

        assertEquals(12, stock(p.getId(), t.getId()));
        assertTrue(notificaciones(p.getId()).isEmpty());
    }

    @Test
    void transferenciaTambienNotificaEnLaTiendaOrigen() {
        var p = producto("1.00");
        var origen = tienda();
        var destino = tienda();
        entrada(p.getId(), origen.getId(), 20);
        entrada(p.getId(), destino.getId(), 1);

        transferenciaService.crearTransferencia(TransferenciaDTO.builder()
                .idProducto(p.getId()).idTiendaOrigen(origen.getId())
                .idTiendaDestino(destino.getId()).cantidad(10).build());

        assertEquals(10, stock(p.getId(), origen.getId()));
        assertEquals(11, stock(p.getId(), destino.getId()));
        List<Notificacion> n = notificaciones(p.getId());
        assertEquals(1, n.size());
        assertEquals(origen.getId(), n.get(0).getIdTienda());
    }

    @Test
    void actualizarVentaSinTiendaSoloCambiaEstado() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 20);
        VentaDTO v = vender(p.getId(), t.getId(), 2);

        VentaDTO actualizada = ventaService.actualizarVenta(v.getId(),
                VentaDTO.builder().estado("ENTREGADA").build());

        assertEquals("ENTREGADA", actualizada.getEstado());
        assertEquals(t.getId(), actualizada.getIdTienda());
        // H2 guarda microsegundos; se compara al milisegundo
        assertEquals(v.getFechaHora().truncatedTo(java.time.temporal.ChronoUnit.MILLIS),
                actualizada.getFechaHora().truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        assertEquals(18, stock(p.getId(), t.getId()));
    }

    @Test
    void actualizarVentaCambiandoTiendaMueveElStock() {
        var p = producto("1.00");
        var t1 = tienda();
        var t2 = tienda();
        entrada(p.getId(), t1.getId(), 20);
        entrada(p.getId(), t2.getId(), 20);
        VentaDTO v = vender(p.getId(), t1.getId(), 5);

        ventaService.actualizarVenta(v.getId(), VentaDTO.builder().idTienda(t2.getId()).build());

        assertEquals(20, stock(p.getId(), t1.getId()));
        assertEquals(15, stock(p.getId(), t2.getId()));
        assertEquals(t2.getId(), salidaService.mostrarPorVenta(v.getId()).get(0).getIdTienda());
    }

    @Test
    void eliminarVentaDevuelveElStock() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 20);
        VentaDTO v = vender(p.getId(), t.getId(), 7);
        assertEquals(13, stock(p.getId(), t.getId()));

        ventaService.eliminarVenta(v.getId());

        assertEquals(20, stock(p.getId(), t.getId()));
        assertTrue(salidaService.mostrarPorVenta(v.getId()).isEmpty());
        assertFalse(ventaRepo.existsById(v.getId()));
    }

    @Test
    void noSePuedeEliminarProductoNiTiendaConMovimientos() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 5);

        assertThrows(ConflictException.class, () -> productoService.eliminarProducto(p.getId()));
        assertThrows(ConflictException.class, () -> tiendaService.eliminarTienda(t.getId()));

        // sin movimientos si se pueden borrar
        var libre = producto("1.00");
        var tiendaLibre = tienda();
        assertDoesNotThrow(() -> productoService.eliminarProducto(libre.getId()));
        assertDoesNotThrow(() -> tiendaService.eliminarTienda(tiendaLibre.getId()));
    }

    @Test
    void reportesStockBajoYMovimientosPaginados() {
        var p = producto("1.00");
        var t = tienda();
        entrada(p.getId(), t.getId(), 5);   // 5 < 12 -> aparece en stock bajo
        entrada(p.getId(), t.getId(), 3);
        vender(p.getId(), t.getId(), 1);

        assertTrue(reporteService.stockBajo().stream()
                .anyMatch(i -> i.getIdProducto().equals(p.getId()) && i.getCantidad() == 7));

        assertEquals(3, reporteService.historialMovimientos(p.getId(), null, null, null).size());
        List<MovimientoDTO> pagina = reporteService.historialMovimientos(p.getId(), t.getId(), 0, 2);
        assertEquals(2, pagina.size());
        assertEquals("SALIDA", pagina.get(0).getTipo()); // mas reciente primero

        assertTrue(reporteService.resumenPorTienda().stream()
                .anyMatch(r -> r.getIdTienda().equals(t.getId())
                        && r.getTotalUnidades() == 7 && r.getProductosDistintos() == 1));
    }
}
