package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Apartado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartadoRepository extends JpaRepository<Apartado, Long> {
    boolean existsByTiendaId(Long tiendaId);
    List<Apartado> findAllByOrderByFechaCreacionDescIdDesc();
}
