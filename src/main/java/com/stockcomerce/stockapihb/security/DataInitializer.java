package com.stockcomerce.stockapihb.security;

import com.stockcomerce.stockapihb.models.AsignacionRol;
import com.stockcomerce.stockapihb.models.Rol;
import com.stockcomerce.stockapihb.models.Usuario;
import com.stockcomerce.stockapihb.repositories.RolRepository;
import com.stockcomerce.stockapihb.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;

// Inicializa el catalogo de roles por riesgo/responsabilidad y el usuario ADMIN semilla.
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private RolRepository rolRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${security.admin.username:admin}")
    private String adminUsername;

    @Value("${security.admin.password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RolCodigo rc : RolCodigo.values()) {
            Rol rol = rolRepo.findByCodigo(rc).orElseGet(() -> Rol.builder().codigo(rc).build());
            rol.setNombre(rc.getNombreVisible());
            rol.setDescripcion(rc.getDescripcion());
            rol.setNivelRiesgo(rc.getNivelRiesgo());
            rol.setTipoAlcance(rc.getTipoAlcance());
            rol.setTipoUbicacionRequerido(rc.getTipoUbicacionRequerido());
            rol.setPermisos(new HashSet<>(rc.getPermisosPorDefecto()));
            rolRepo.save(rol);
        }

        if (!usuarioRepo.existsByUsername(adminUsername)) {
            Rol rolAdmin = rolRepo.findByCodigo(RolCodigo.ADMIN).orElseThrow();
            Usuario admin = Usuario.builder()
                    .username(adminUsername)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .nombreCompleto("Administrador Inicial")
                    .activo(true)
                    .asignaciones(new ArrayList<>())
                    .build();
            admin.getAsignaciones().add(AsignacionRol.builder()
                    .usuario(admin)
                    .rol(rolAdmin)
                    .activo(true)
                    .build());
            usuarioRepo.save(admin);
        }
    }
}
