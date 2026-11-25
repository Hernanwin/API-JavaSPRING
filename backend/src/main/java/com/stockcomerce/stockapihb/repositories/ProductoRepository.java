package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByProveedorId(Long proveedorId);
    List<Producto> findByProveedorId(Long proveedorId);
}
