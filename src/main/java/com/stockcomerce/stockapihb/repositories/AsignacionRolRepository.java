package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.AsignacionRol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionRolRepository extends JpaRepository<AsignacionRol, Long> {
    boolean existsByTiendaId(Long idTienda);
}
