package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findAllByOrderByFechaDescIdDesc();
    List<Notificacion> findByLeidaFalseOrderByFechaDescIdDesc();
    List<Notificacion> findByIdProductoOrderByIdAsc(Long idProducto);

    @Modifying
    @Query("update Notificacion n set n.leida = true where n.leida = false")
    int marcarTodasLeidas();
}
