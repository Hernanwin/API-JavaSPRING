package com.stockcomerce.stockapihb.repositories;

import com.stockcomerce.stockapihb.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("select distinct u from Usuario u " +
            "left join fetch u.asignaciones a " +
            "left join fetch a.rol r " +
            "left join fetch a.tienda t " +
            "where u.username = :username")
    Optional<Usuario> findConAsignacionesByUsername(@Param("username") String username);
}
