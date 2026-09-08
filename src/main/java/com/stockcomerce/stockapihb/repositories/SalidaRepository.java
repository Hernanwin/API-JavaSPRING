package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Salida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalidaRepository extends JpaRepository<Salida, Long> {
    List<Salida> findByTiendaId(Long idTienda);
    List<Salida> findByVentaId(Long idVenta);
}
