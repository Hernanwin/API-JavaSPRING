package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository <Tienda, Long> {
}