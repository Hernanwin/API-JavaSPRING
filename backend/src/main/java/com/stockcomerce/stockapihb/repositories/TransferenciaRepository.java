package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findByTiendaOrigenId(Long idTienda);
    List<Transferencia> findByTiendaDestinoId(Long idTienda);
    boolean existsByProductoId(Long idProducto);
    boolean existsByTiendaOrigenIdOrTiendaDestinoId(Long idOrigen, Long idDestino);

    // una transferencia "toca" tanto la tienda origen como la destino
    @Query("select tr from Transferencia tr join fetch tr.producto p " +
            "join fetch tr.tiendaOrigen o join fetch tr.tiendaDestino d " +
            "where (:idProducto is null or p.id = :idProducto) " +
            "and (:idTienda is null or o.id = :idTienda or d.id = :idTienda)")
    List<Transferencia> filtrar(@Param("idProducto") Long idProducto, @Param("idTienda") Long idTienda);
}
