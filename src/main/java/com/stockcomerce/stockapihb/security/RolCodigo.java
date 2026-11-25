package com.stockcomerce.stockapihb.security;

import com.stockcomerce.stockapihb.models.TipoUbicacion;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum RolCodigo {

    ENCARGADO_DEPOSITO(
            "Encargado de depósito",
            "Custodia operativa de depósito(s) asignado(s): entradas, salidas, envíos/recepciones y ajustes por conteo, sin modificar precios ni usuarios.",
            NivelRiesgo.MEDIO,
            TipoAlcance.UBICACION,
            TipoUbicacion.DEPOSITO,
            EnumSet.of(
                    Permiso.CATALOGO_VER,
                    Permiso.INVENTARIO_VER,
                    Permiso.ENTRADA_REGISTRAR,
                    Permiso.SALIDA_MANUAL_REGISTRAR,
                    Permiso.TRANSFERENCIA_SOLICITAR,
                    Permiso.TRANSFERENCIA_OPERAR,
                    Permiso.AJUSTE_CONTEO_EJECUTAR,
                    Permiso.ORDEN_COMPRA_VER,
                    Permiso.REPORTE_OPERATIVO_VER
            )
    ),

    CAJERO_VENDEDOR(
            "Cajero / Vendedor",
            "Operación de POS en su caja/sucursal: cobros, consulta de stock disponible sin costos y gestión de apartados, sin reportes consolidados ni configuración.",
            NivelRiesgo.OPERATIVO,
            TipoAlcance.UBICACION,
            TipoUbicacion.TIENDA,
            EnumSet.of(
                    Permiso.CATALOGO_VER,
                    Permiso.INVENTARIO_VER,
                    Permiso.POS_COBRAR,
                    Permiso.APARTADO_GESTIONAR
            )
    ),

    SUPERVISOR_COMPRAS(
            "Supervisor de compras",
            "Visión consolidada y por proveedor, aprobación de órdenes de compra y transferencias, seguimiento de recepción y reportes de rotación y márgenes, sin operar caja.",
            NivelRiesgo.ALTO,
            TipoAlcance.GLOBAL,
            null,
            EnumSet.of(
                    Permiso.CATALOGO_VER,
                    Permiso.COSTOS_VER,
                    Permiso.INVENTARIO_VER,
                    Permiso.STOCK_MINIMO_CONFIGURAR,
                    Permiso.PROVEEDOR_GESTIONAR,
                    Permiso.ORDEN_COMPRA_VER,
                    Permiso.ORDEN_COMPRA_CREAR,
                    Permiso.ORDEN_COMPRA_APROBAR,
                    Permiso.TRANSFERENCIA_SOLICITAR,
                    Permiso.TRANSFERENCIA_APROBAR,
                    Permiso.REPORTE_OPERATIVO_VER,
                    Permiso.REPORTE_ROTACION_MARGENES_VER
            )
    ),

    ADMIN(
            "Administrador general",
            "Gobierno total de configuración, catálogo/precios, usuarios, sucursales y reportes financieros consolidados.",
            NivelRiesgo.CRITICO,
            TipoAlcance.GLOBAL,
            null,
            EnumSet.allOf(Permiso.class)
    );

    private final String nombreVisible;
    private final String descripcion;
    private final NivelRiesgo nivelRiesgo;
    private final TipoAlcance tipoAlcance;
    // Si no es null, restringe que tipo de ubicacion admite la asignacion (ej. solo DEPOSITO o solo TIENDA)
    private final TipoUbicacion tipoUbicacionRequerido;
    private final Set<Permiso> permisosPorDefecto;

    RolCodigo(String nombreVisible,
              String descripcion,
              NivelRiesgo nivelRiesgo,
              TipoAlcance tipoAlcance,
              TipoUbicacion tipoUbicacionRequerido,
              Set<Permiso> permisosPorDefecto) {
        this.nombreVisible = nombreVisible;
        this.descripcion = descripcion;
        this.nivelRiesgo = nivelRiesgo;
        this.tipoAlcance = tipoAlcance;
        this.tipoUbicacionRequerido = tipoUbicacionRequerido;
        this.permisosPorDefecto = Set.copyOf(permisosPorDefecto);
    }
}
