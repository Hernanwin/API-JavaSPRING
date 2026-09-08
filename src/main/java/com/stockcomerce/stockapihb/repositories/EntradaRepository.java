package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    List<Entrada> findByTiendaId(Long idTienda);
    List<Entrada> findByProductoId(Long idProducto);
}
