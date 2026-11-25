package com.stockcomerce.stockapihb.security;

import java.util.Collections;
import java.util.Set;

// Representa el filtro de alcance de datos efectivo de un usuario para una operacion dada.
public record AlcanceUbicacion(boolean global, Set<Long> idsTiendas) {

    public static AlcanceUbicacion accesoGlobal() {
        return new AlcanceUbicacion(true, Collections.emptySet());
    }

    public static AlcanceUbicacion restringido(Set<Long> ids) {
        return new AlcanceUbicacion(false, Set.copyOf(ids));
    }

    public boolean incluye(Long idTienda) {
        return global || (idTienda != null && idsTiendas.contains(idTienda));
    }

    public boolean incluyeAlguna(Long idTienda1, Long idTienda2) {
        return global || incluye(idTienda1) || incluye(idTienda2);
    }
}
