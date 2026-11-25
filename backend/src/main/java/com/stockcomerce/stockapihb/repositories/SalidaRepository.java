package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Salida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalidaRepository extends JpaRepository<Salida, Long> {
    List<Salida> findByTiendaId(Long idTienda);
    List<Salida> findByVentaId(Long idVenta);
    boolean existsByProductoId(Long idProducto);
    boolean existsByTiendaId(Long idTienda);

    // filtros opcionales: si el parametro es null no filtra por ese campo
    @Query("select s from Salida s join fetch s.producto p join fetch s.tienda t " +
            "where (:idProducto is null or p.id = :idProducto) " +
            "and (:idTienda is null or t.id = :idTienda)")
    List<Salida> filtrar(@Param("idProducto") Long idProducto, @Param("idTienda") Long idTienda);
}
