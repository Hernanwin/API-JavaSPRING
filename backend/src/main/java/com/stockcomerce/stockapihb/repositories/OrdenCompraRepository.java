package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    boolean existsByProveedorId(Long proveedorId);
    boolean existsByTiendaDestinoId(Long tiendaDestinoId);
    List<OrdenCompra> findAllByOrderByFechaCreacionDescIdDesc();
}
