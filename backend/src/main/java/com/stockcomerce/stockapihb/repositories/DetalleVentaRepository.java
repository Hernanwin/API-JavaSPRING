package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    boolean existsByProductoId(Long idProducto);
}
