package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.AjusteConteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AjusteConteoRepository extends JpaRepository<AjusteConteo, Long> {
    boolean existsByProductoId(Long productoId);
    boolean existsByTiendaId(Long tiendaId);
    List<AjusteConteo> findAllByOrderByFechaDescIdDesc();

    @Query("select a from AjusteConteo a join fetch a.producto p join fetch a.tienda t " +
            "where (:idProducto is null or p.id = :idProducto) " +
            "and (:idTienda is null or t.id = :idTienda)")
    List<AjusteConteo> filtrar(@Param("idProducto") Long idProducto, @Param("idTienda") Long idTienda);
}
