package com.stockcomerce.stockapihb.events;

// Se publica desde InventarioService cuando el stock de un Producto en una Tienda
// cruza hacia abajo el stock minimo (p.ej. pasa de 12 a 11 con minimo 12).
// Lleva datos planos (no entidades) para poder procesarse fuera de la transaccion.
public record StockBajoEvent(
        Long idProducto,
        String nombreProducto,
        Long idTienda,
        String nombreTienda,
        int cantidadActual,
        int stockMinimo
) {
}
