package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProductoIdAndTiendaId(Long productoId, Long tiendaId);
    List<Inventario> findByTiendaId(Long tiendaId);
    List<Inventario> findByProductoId(Long productoId);
    boolean existsByProductoIdAndTiendaId(Long productoId, Long tiendaId);
    boolean existsByProductoId(Long productoId);
    boolean existsByTiendaId(Long tiendaId);

    // reporte de stock bajo resuelto en la base (antes: findAll + filtro en memoria)
    @Query("select i from Inventario i join fetch i.producto join fetch i.tienda " +
            "where i.stockMinimo is not null and i.cantidad < i.stockMinimo " +
            "order by i.tienda.nombre, i.producto.nombre")
    List<Inventario> findStockBajo();

    // [idTienda, nombreTienda, totalUnidades, productosDistintos] agrupado por tienda
    @Query("select t.id, t.nombre, coalesce(sum(i.cantidad), 0), count(i) " +
            "from Inventario i join i.tienda t group by t.id, t.nombre order by t.nombre")
    List<Object[]> resumenPorTienda();
}
