package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository <Venta, Long> {
}