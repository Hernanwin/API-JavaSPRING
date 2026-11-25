package com.stockcomerce.stockapihb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RbacAlcanceIntegrationTest {

    @LocalServerPort
    private int port;

    private HttpClient http;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        http = HttpClient.newHttpClient();
        adminToken = login("admin", "admin123");
    }

    // ---------- HTTP helpers ----------

    private HttpResponse<String> send(String method, String path, String token, String jsonBody) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        if ("POST".equals(method)) {
            b.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
        } else if ("PUT".equals(method)) {
            b.PUT(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
        } else if ("DELETE".equals(method)) {
            b.DELETE();
        } else {
            b.GET();
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String username, String password) throws Exception {
        HttpResponse<String> res = send("POST", "/api/auth/login", null,
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
        assertEquals(200, res.statusCode(), () -> "Login fallo: " + res.body());
        return extraerTexto(res.body(), "token");
    }

    private long crearUbicacion(String prefijo, String tipo) throws Exception {
        String nombre = prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> res = send("POST", "/api/tiendas", adminToken,
                "{\"nombre\":\"" + nombre + "\",\"direccion\":\"Calle 123\",\"tipo\":\"" + tipo + "\"}");
        assertEquals(201, res.statusCode(), res.body());
        return extraerLong(res.body(), "id");
    }

    private long crearProveedor(String prefijo) throws Exception {
        String nombre = prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> res = send("POST", "/api/proveedores", adminToken,
                "{\"nombre\":\"" + nombre + "\",\"identificacionFiscal\":\"30-12345678-9\"}");
        assertEquals(201, res.statusCode(), res.body());
        return extraerLong(res.body(), "id");
    }

    private long crearProducto(String prefijo, String precio, String costo, Long idProveedor) throws Exception {
        String nombre = prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
        String provField = idProveedor != null ? ",\"idProveedor\":" + idProveedor : "";
        HttpResponse<String> res = send("POST", "/api/productos", adminToken,
                "{\"nombre\":\"" + nombre + "\",\"precio\":" + precio + ",\"costo\":" + costo
                        + ",\"categoria\":\"General\"" + provField + "}");
        assertEquals(201, res.statusCode(), res.body());
        return extraerLong(res.body(), "id");
    }

    private String crearUsuarioYLogin(String prefijo, String asignacionesJson) throws Exception {
        String username = prefijo + "_" + UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> res = send("POST", "/api/usuarios", adminToken,
                "{\"username\":\"" + username + "\",\"password\":\"clave123\","
                        + "\"nombreCompleto\":\"Usuario " + prefijo + "\","
                        + "\"asignaciones\":" + asignacionesJson + "}");
        assertEquals(201, res.statusCode(), () -> "No se pudo crear usuario: " + res.body());
        return login(username, "clave123");
    }

    private static long extraerLong(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(m.find(), () -> "No se encontro '" + campo + "' en: " + json);
        return Long.parseLong(m.group(1));
    }

    private static String extraerTexto(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(m.find(), () -> "No se encontro '" + campo + "' en: " + json);
        return m.group(1);
    }

    // ---------- Tests ----------

    @Test
    void accesoSinTokenEsRechazadoYAsignacionesInvalidasPorTipoDeUbicacionFallan() throws Exception {
        HttpResponse<String> anon = send("GET", "/api/productos", null, null);
        assertEquals(401, anon.statusCode());

        long idTiendaComercial = crearUbicacion("Sucursal", "TIENDA");
        long idDeposito = crearUbicacion("Deposito", "DEPOSITO");

        // Encargado de deposito NO se puede asignar a una TIENDA comercial
        HttpResponse<String> depEnTienda = send("POST", "/api/usuarios", adminToken,
                "{\"username\":\"err1_" + UUID.randomUUID().toString().substring(0, 6) + "\","
                        + "\"password\":\"1234\",\"nombreCompleto\":\"Err 1\","
                        + "\"asignaciones\":[{\"rol\":\"ENCARGADO_DEPOSITO\",\"idTienda\":" + idTiendaComercial + "}]}");
        assertEquals(400, depEnTienda.statusCode(), depEnTienda.body());

        // Cajero/vendedor NO se puede asignar a un DEPOSITO
        HttpResponse<String> cajEnDeposito = send("POST", "/api/usuarios", adminToken,
                "{\"username\":\"err2_" + UUID.randomUUID().toString().substring(0, 6) + "\","
                        + "\"password\":\"1234\",\"nombreCompleto\":\"Err 2\","
                        + "\"asignaciones\":[{\"rol\":\"CAJERO_VENDEDOR\",\"idTienda\":" + idDeposito + "}]}");
        assertEquals(400, cajEnDeposito.statusCode(), cajEnDeposito.body());
    }

    @Test
    void encargadoDepositoOperaSoloSusDepositosSinModificarPreciosNiUsuarios() throws Exception {
        long dep1 = crearUbicacion("DepNorte", "DEPOSITO");
        long dep2 = crearUbicacion("DepCentral", "DEPOSITO");
        long depAjeno = crearUbicacion("DepSurAjeno", "DEPOSITO");
        long prod = crearProducto("Taladro", "150.00", "90.00", null);

        // Stock en deposito ajeno (no debe filtrarse al encargado)
        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + depAjeno + ",\"cantidad\":50}");

        // El encargado tiene acceso a DOS depositos (dep1 y dep2), pero NO a depAjeno
        String tokenEncargado = crearUsuarioYLogin("encargado",
                "[{\"rol\":\"ENCARGADO_DEPOSITO\",\"idTienda\":" + dep1 + "},"
                        + "{\"rol\":\"ENCARGADO_DEPOSITO\",\"idTienda\":" + dep2 + "}]");

        // 1. Puede ver catalogo pero SIN costos
        HttpResponse<String> cat = send("GET", "/api/productos", tokenEncargado, null);
        assertEquals(200, cat.statusCode());
        assertFalse(cat.body().contains("\"costo\""), "El encargado no debe ver costos: " + cat.body());

        // 2. NO puede modificar precios ni crear usuarios
        HttpResponse<String> modPrecio = send("PUT", "/api/productos/" + prod, tokenEncargado,
                "{\"nombre\":\"Taladro\",\"precio\":199.99,\"categoria\":\"General\"}");
        assertEquals(403, modPrecio.statusCode());

        HttpResponse<String> crearUsr = send("POST", "/api/usuarios", tokenEncargado,
                "{\"username\":\"hacker\",\"password\":\"123\",\"nombreCompleto\":\"H\","
                        + "\"asignaciones\":[{\"rol\":\"ADMIN\"}]}");
        assertEquals(403, crearUsr.statusCode());

        // 3. Registra entradas, salidas operativas y ajustes por conteo en sus depositos asignados
        assertEquals(201, send("POST", "/api/entradas", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + dep1 + ",\"cantidad\":30}").statusCode());
        assertEquals(201, send("POST", "/api/entradas", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + dep2 + ",\"cantidad\":20}").statusCode());

        assertEquals(201, send("POST", "/api/salidas", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + dep1 + ",\"cantidad\":4,\"motivo\":\"MERMA\"}").statusCode());

        HttpResponse<String> conteo = send("POST", "/api/ajustes-conteo", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + dep1 + ",\"cantidadContada\":25,\"motivo\":\"CONTEO_CICLICO\"}");
        assertEquals(201, conteo.statusCode(), conteo.body());
        assertTrue(conteo.body().contains("\"diferencia\":-1"));

        // 4. En el deposito ajeno recibe 403 Forbidden
        assertEquals(403, send("POST", "/api/entradas", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + depAjeno + ",\"cantidad\":10}").statusCode());
        assertEquals(403, send("POST", "/api/ajustes-conteo", tokenEncargado,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + depAjeno + ",\"cantidadContada\":10}").statusCode());

        // 5. Filtrado automatico de datos en inventario: ve dep1 y dep2, nunca depAjeno, y sin costos
        HttpResponse<String> inv = send("GET", "/api/inventario", tokenEncargado, null);
        assertEquals(200, inv.statusCode());
        assertTrue(inv.body().contains("\"idTienda\":" + dep1));
        assertTrue(inv.body().contains("\"idTienda\":" + dep2));
        assertFalse(inv.body().contains("\"idTienda\":" + depAjeno));
        assertFalse(inv.body().contains("\"costoUnitario\""));
    }

    @Test
    void cajeroVendedorOperaPosYApartadosEnSuSucursalSinCostosNiReportesConsolidados() throws Exception {
        long tiendaPropia = crearUbicacion("TiendaCentro", "TIENDA");
        long tiendaAjena = crearUbicacion("TiendaNorte", "TIENDA");
        long prod = crearProducto("Zapatilla", "100.00", "45.00", null);

        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + tiendaPropia + ",\"cantidad\":10}");
        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + tiendaAjena + ",\"cantidad\":15}");

        String tokenCajero = crearUsuarioYLogin("cajero",
                "[{\"rol\":\"CAJERO_VENDEDOR\",\"idTienda\":" + tiendaPropia + ",\"caja\":\"CAJA-02\"}]");

        // 1. Consulta stock de su tienda SIN costos; bloqueado en tienda ajena
        HttpResponse<String> inv = send("GET", "/api/inventario", tokenCajero, null);
        assertEquals(200, inv.statusCode());
        assertTrue(inv.body().contains("\"idTienda\":" + tiendaPropia));
        assertFalse(inv.body().contains("\"idTienda\":" + tiendaAjena));
        assertFalse(inv.body().contains("\"costoUnitario\""), "El cajero no debe ver costos: " + inv.body());
        assertFalse(inv.body().contains("\"valorCostoTotal\""));

        assertEquals(403, send("GET", "/api/inventario/tienda/" + tiendaAjena, tokenCajero, null).statusCode());

        // 2. Puede cobrar en su tienda (asocia automaticamente su caja), pero NO en otra sucursal
        HttpResponse<String> ventaOk = send("POST", "/api/ventas", tokenCajero,
                "{\"idTienda\":" + tiendaPropia + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":2}]}");
        assertEquals(201, ventaOk.statusCode(), ventaOk.body());
        assertTrue(ventaOk.body().contains("\"caja\":\"CAJA-02\""));

        assertEquals(403, send("POST", "/api/ventas", tokenCajero,
                "{\"idTienda\":" + tiendaAjena + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":1}]}").statusCode());

        // 3. Genera apartado en su tienda -> reserva stock e impide sobrevender esas unidades
        // Stock fisico quedo en 8 (10 - 2). Aparta 6 -> quedan solo 2 disponibles.
        HttpResponse<String> apRes = send("POST", "/api/apartados", tokenCajero,
                "{\"idTienda\":" + tiendaPropia + ",\"clienteNombre\":\"Maria Lopez\","
                        + "\"montoAbonado\":150.00,"
                        + "\"detalle\":[{\"idProducto\":" + prod + ",\"cantidad\":6}]}");
        assertEquals(201, apRes.statusCode(), apRes.body());
        long idApartado = extraerLong(apRes.body(), "id");
        assertTrue(apRes.body().contains("\"saldoPendiente\":450.00"));

        // Intentar vender 3 unidades falla porque solo hay 2 disponibles (6 estan apartadas)
        HttpResponse<String> sobreventa = send("POST", "/api/ventas", tokenCajero,
                "{\"idTienda\":" + tiendaPropia + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":3}]}");
        assertEquals(400, sobreventa.statusCode(), sobreventa.body());

        // Cobrar el apartado libera la reserva y registra la venta definitiva (stock fisico pasa de 8 a 2)
        HttpResponse<String> cobroAp = send("POST", "/api/apartados/" + idApartado + "/cobrar", tokenCajero, "");
        assertEquals(200, cobroAp.statusCode(), cobroAp.body());
        assertTrue(cobroAp.body().contains("\"estado\":\"COMPLETADO\""));

        HttpResponse<String> stockFinal = send("GET",
                "/api/inventario/stock?idProducto=" + prod + "&idTienda=" + tiendaPropia, tokenCajero, null);
        assertTrue(stockFinal.body().contains("\"cantidad\":2"));
        assertTrue(stockFinal.body().contains("\"cantidadReservada\":0"));

        // 4. Sin acceso a reportes financieros consolidados, margenes ni configuracion
        assertEquals(403, send("GET", "/api/reportes/financiero-consolidado", tokenCajero, null).statusCode());
        assertEquals(403, send("GET", "/api/reportes/margenes", tokenCajero, null).statusCode());
        assertEquals(403, send("POST", "/api/tiendas", tokenCajero,
                "{\"nombre\":\"SucursalPirata\",\"direccion\":\"X\"}").statusCode());
        assertEquals(403, send("GET", "/api/usuarios", tokenCajero, null).statusCode());
    }

    @Test
    void supervisorComprasApruebaOrdenesYTransferenciasYVeMargenesPeroNoOperaCaja() throws Exception {
        long depOrigen = crearUbicacion("DepositoCentral", "DEPOSITO");
        long tiendaDestino = crearUbicacion("SucursalOeste", "TIENDA");
        long prov = crearProveedor("ProveedorTextil");
        long prod = crearProducto("Campera", "200.00", "120.00", prov);

        String tokenSupervisor = crearUsuarioYLogin("supervisor",
                "[{\"rol\":\"SUPERVISOR_COMPRAS\"}]");
        String tokenEncargadoOrigen = crearUsuarioYLogin("enc_origen",
                "[{\"rol\":\"ENCARGADO_DEPOSITO\",\"idTienda\":" + depOrigen + "}]");

        // 1. Supervisor crea y aprueba Orden de Compra hacia el deposito origen
        HttpResponse<String> ocCreada = send("POST", "/api/ordenes-compra", tokenSupervisor,
                "{\"idProveedor\":" + prov + ",\"idTiendaDestino\":" + depOrigen + ","
                        + "\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadSolicitada\":20,\"costoUnitario\":120.00}]}");
        assertEquals(201, ocCreada.statusCode(), ocCreada.body());
        long idOc = extraerLong(ocCreada.body(), "id");
        assertTrue(ocCreada.body().contains("\"estado\":\"PENDIENTE_APROBACION\""));

        // Encargado NO puede aprobar la OC
        assertEquals(403, send("PUT", "/api/ordenes-compra/" + idOc + "/aprobar", tokenEncargadoOrigen, "").statusCode());

        // Supervisor SI aprueba la OC
        HttpResponse<String> ocAprob = send("PUT", "/api/ordenes-compra/" + idOc + "/aprobar", tokenSupervisor, "");
        assertEquals(200, ocAprob.statusCode(), ocAprob.body());
        assertTrue(ocAprob.body().contains("\"estado\":\"APROBADA\""));

        // Encargado del deposito recibe parcialmente (15 de 20) y no ve costos en la respuesta
        HttpResponse<String> ocRec = send("POST", "/api/ordenes-compra/" + idOc + "/recepcion", tokenEncargadoOrigen,
                "{\"items\":[{\"idProducto\":" + prod + ",\"cantidadRecibida\":15}],\"observacion\":\"Faltaron 5\"}");
        assertEquals(200, ocRec.statusCode(), ocRec.body());
        assertTrue(ocRec.body().contains("\"estado\":\"RECIBIDA_PARCIAL\""));
        assertTrue(ocRec.body().contains("\"porcentajeCumplimiento\":75.00"));
        assertFalse(ocRec.body().contains("\"costoUnitario\""));

        // 2. Flujo completo de Transferencia: Encargado solicita -> Supervisor aprueba -> Encargado envia -> Admin/Tienda recibe
        HttpResponse<String> trSol = send("POST", "/api/transferencias/solicitar", tokenEncargadoOrigen,
                "{\"idProducto\":" + prod + ",\"idTiendaOrigen\":" + depOrigen
                        + ",\"idTiendaDestino\":" + tiendaDestino + ",\"cantidad\":10}");
        assertEquals(201, trSol.statusCode(), trSol.body());
        long idTr = extraerLong(trSol.body(), "id");

        // Encargado no puede auto-aprobar su transferencia
        assertEquals(403, send("PUT", "/api/transferencias/" + idTr + "/aprobar", tokenEncargadoOrigen, "").statusCode());

        // Supervisor aprueba la transferencia
        assertEquals(200, send("PUT", "/api/transferencias/" + idTr + "/aprobar", tokenSupervisor, "").statusCode());

        // Encargado despacha desde depOrigen (stock baja de 15 a 5)
        assertEquals(200, send("PUT", "/api/transferencias/" + idTr + "/enviar", tokenEncargadoOrigen, "").statusCode());

        // Recepcion en tiendaDestino (reciben 10 unidades)
        assertEquals(200, send("PUT", "/api/transferencias/" + idTr + "/recibir?cantidadRecibida=10", adminToken, "").statusCode());

        // 3. Supervisor consulta inventario consolidado, por proveedor y reportes de rotacion, margenes y cumplimiento
        HttpResponse<String> invCons = send("GET", "/api/inventario/consolidado?idProveedor=" + prov, tokenSupervisor, null);
        assertEquals(200, invCons.statusCode(), invCons.body());
        assertTrue(invCons.body().contains("\"totalUnidades\":15"));
        assertTrue(invCons.body().contains("\"costoUnitario\":120.00"));

        assertEquals(200, send("GET", "/api/inventario/proveedor/" + prov, tokenSupervisor, null).statusCode());
        assertEquals(200, send("GET", "/api/reportes/margenes?idProveedor=" + prov, tokenSupervisor, null).statusCode());
        assertEquals(200, send("GET", "/api/reportes/rotacion?idProveedor=" + prov, tokenSupervisor, null).statusCode());

        HttpResponse<String> cumpl = send("GET", "/api/reportes/cumplimiento-recepcion?idProveedor=" + prov, tokenSupervisor, null);
        assertEquals(200, cumpl.statusCode(), cumpl.body());
        assertTrue(cumpl.body().contains("\"porcentajeCumplimientoOc\":75.00"));

        // 4. Supervisor NO puede operar la caja/POS ni ver el reporte financiero consolidado de direccion
        assertEquals(403, send("POST", "/api/ventas", tokenSupervisor,
                "{\"idTienda\":" + tiendaDestino + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":1}]}").statusCode());
        assertEquals(403, send("POST", "/api/apartados", tokenSupervisor,
                "{\"idTienda\":" + tiendaDestino + ",\"clienteNombre\":\"X\","
                        + "\"detalle\":[{\"idProducto\":" + prod + ",\"cantidad\":1}]}").statusCode());
        assertEquals(403, send("GET", "/api/reportes/financiero-consolidado", tokenSupervisor, null).statusCode());

        // El ADMIN si accede al financiero consolidado
        assertEquals(200, send("GET", "/api/reportes/financiero-consolidado", adminToken, null).statusCode());
    }

    @Test
    void usuarioMultiUbicacionRespetaElRolEspecificoEnCadaUbicacion() throws Exception {
        long tiendaA = crearUbicacion("SucursalA", "TIENDA");
        long depositoB = crearUbicacion("DepositoB", "DEPOSITO");
        long tiendaExterna = crearUbicacion("SucursalExterna", "TIENDA");
        long prod = crearProducto("Teclado", "50.00", "25.00", null);

        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + tiendaA + ",\"cantidad\":10}");
        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + depositoB + ",\"cantidad\":20}");
        send("POST", "/api/entradas", adminToken,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + tiendaExterna + ",\"cantidad\":30}");

        // Usuario mixto: Cajero en tiendaA + Encargado de deposito en depositoB
        String tokenMixto = crearUsuarioYLogin("mixto",
                "[{\"rol\":\"CAJERO_VENDEDOR\",\"idTienda\":" + tiendaA + ",\"caja\":\"CAJA-A\"},"
                        + "{\"rol\":\"ENCARGADO_DEPOSITO\",\"idTienda\":" + depositoB + "}]");

        // Puede cobrar en tiendaA, pero NO en depositoB (alli es encargado, no cajero)
        assertEquals(201, send("POST", "/api/ventas", tokenMixto,
                "{\"idTienda\":" + tiendaA + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":1}]}").statusCode());
        assertEquals(403, send("POST", "/api/ventas", tokenMixto,
                "{\"idTienda\":" + depositoB + ",\"detalle\":[{\"idProducto\":" + prod + ",\"cantidadProd\":1}]}").statusCode());

        // Puede ajustar conteo en depositoB, pero NO en tiendaA (alli es cajero, no encargado)
        assertEquals(201, send("POST", "/api/ajustes-conteo", tokenMixto,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + depositoB + ",\"cantidadContada\":19}").statusCode());
        assertEquals(403, send("POST", "/api/ajustes-conteo", tokenMixto,
                "{\"idProducto\":" + prod + ",\"idTienda\":" + tiendaA + ",\"cantidadContada\":9}").statusCode());

        // Inventario le devuelve automaticamente tiendaA y depositoB, ocultando tiendaExterna
        HttpResponse<String> inv = send("GET", "/api/inventario", tokenMixto, null);
        assertEquals(200, inv.statusCode());
        assertTrue(inv.body().contains("\"idTienda\":" + tiendaA));
        assertTrue(inv.body().contains("\"idTienda\":" + depositoB));
        assertFalse(inv.body().contains("\"idTienda\":" + tiendaExterna));
    }
}
