package com.stockcomerce.stockapihb.security;

public enum TipoAlcance {
    // Aplica sobre todas las sucursales y depositos (ej. ADMIN, SUPERVISOR_COMPRAS)
    GLOBAL,
    // Restringido estrictamente a las ubicaciones (Tienda/Deposito) vinculadas en la asignacion
    UBICACION
}
