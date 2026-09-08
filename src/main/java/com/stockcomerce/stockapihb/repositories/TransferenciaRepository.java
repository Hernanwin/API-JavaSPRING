package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findByTiendaOrigenId(Long idTienda);
    List<Transferencia> findByTiendaDestinoId(Long idTienda);
}
