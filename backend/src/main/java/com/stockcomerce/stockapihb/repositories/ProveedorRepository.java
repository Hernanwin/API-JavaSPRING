package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
