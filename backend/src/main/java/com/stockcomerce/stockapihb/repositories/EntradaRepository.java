package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    List<Entrada> findByTiendaId(Long idTienda);
    List<Entrada> findByProductoId(Long idProducto);
    boolean existsByProductoId(Long idProducto);
    boolean existsByTiendaId(Long idTienda);

    // filtros opcionales: si el parametro es null no filtra por ese campo
    @Query("select e from Entrada e join fetch e.producto p join fetch e.tienda t " +
            "where (:idProducto is null or p.id = :idProducto) " +
            "and (:idTienda is null or t.id = :idTienda)")
    List<Entrada> filtrar(@Param("idProducto") Long idProducto, @Param("idTienda") Long idTienda);
}
