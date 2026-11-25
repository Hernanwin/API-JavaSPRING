package com.stockcomerce.stockapihb.exception;

// operacion valida en forma pero que choca con el estado actual de los datos
// (p.ej. borrar un producto que ya tiene movimientos de stock) -> HTTP 409
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
