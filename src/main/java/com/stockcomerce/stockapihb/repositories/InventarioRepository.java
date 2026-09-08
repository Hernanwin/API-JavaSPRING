package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProductoIdAndTiendaId(Long productoId, Long tiendaId);
    List<Inventario> findByTiendaId(Long tiendaId);
    List<Inventario> findByProductoId(Long productoId);
    boolean existsByProductoIdAndTiendaId(Long productoId, Long tiendaId);
}
