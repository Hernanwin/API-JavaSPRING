package com.stockcomerce.stockapihb.security;

// Capacidades modeladas por riesgo y responsabilidad operativa (no por pantallas).
public enum Permiso {
    // Catalogo, precios y costos (riesgo comercial / confidencialidad de margenes)
    CATALOGO_VER,
    COSTOS_VER,
    CATALOGO_GESTIONAR,
    PRECIOS_MODIFICAR,

    // Custodia fisica de inventario y operatoria de deposito
    INVENTARIO_VER,
    STOCK_MINIMO_CONFIGURAR,
    ENTRADA_REGISTRAR,
    SALIDA_MANUAL_REGISTRAR,
    TRANSFERENCIA_SOLICITAR,
    TRANSFERENCIA_APROBAR,
    TRANSFERENCIA_OPERAR,
    AJUSTE_CONTEO_EJECUTAR,

    // Punto de venta (POS), caja y apartados en tienda
    POS_COBRAR,
    VENTA_ADMINISTRAR,
    APARTADO_GESTIONAR,

    // Abastecimiento, proveedores y ordenes de compra
    PROVEEDOR_GESTIONAR,
    ORDEN_COMPRA_VER,
    ORDEN_COMPRA_CREAR,
    ORDEN_COMPRA_APROBAR,

    // Analitica y reportes segun sensibilidad financiera
    REPORTE_OPERATIVO_VER,
    REPORTE_ROTACION_MARGENES_VER,
    REPORTE_FINANCIERO_CONSOLIDADO_VER,

    // Gobierno de la plataforma y configuracion critica
    TIENDA_CONFIGURAR,
    USUARIO_GESTIONAR
}
