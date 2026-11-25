package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProveedorDTO;
import com.stockcomerce.stockapihb.exception.ConflictException;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Proveedor;
import com.stockcomerce.stockapihb.repositories.OrdenCompraRepository;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.ProveedorRepository;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService implements IProveedorService {

    @Autowired
    private ProveedorRepository repo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private OrdenCompraRepository ordenCompraRepo;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> mostrarProveedores() {
        accessControl.exigirPermiso(Permiso.PROVEEDOR_GESTIONAR, Permiso.ORDEN_COMPRA_VER);
        return repo.findAll().stream().map(Mapper::proveedorDTO).toList();
    }

    @Override
    @Transactional
    public ProveedorDTO crearProveedor(ProveedorDTO dto) {
        accessControl.exigirPermiso(Permiso.PROVEEDOR_GESTIONAR);
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        String limpio = dto.getNombre().trim();
        if (repo.existsByNombreIgnoreCase(limpio)) {
            throw new ConflictException("Ya existe un proveedor con nombre '" + limpio + "'");
        }
        Proveedor p = Proveedor.builder()
                .nombre(limpio)
                .identificacionFiscal(dto.getIdentificacionFiscal())
                .contacto(dto.getContacto())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .activo(dto.getActivo() == null || dto.getActivo())
                .build();
        return Mapper.proveedorDTO(repo.save(p));
    }

    @Override
    @Transactional
    public ProveedorDTO actualizarProveedor(Long id, ProveedorDTO dto) {
        accessControl.exigirPermiso(Permiso.PROVEEDOR_GESTIONAR);
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        p.setNombre(dto.getNombre().trim());
        p.setIdentificacionFiscal(dto.getIdentificacionFiscal());
        p.setContacto(dto.getContacto());
        p.setEmail(dto.getEmail());
        p.setTelefono(dto.getTelefono());
        if (dto.getActivo() != null) {
            p.setActivo(dto.getActivo());
        }
        return Mapper.proveedorDTO(repo.save(p));
    }

    @Override
    @Transactional
    public void eliminarProveedor(Long id) {
        accessControl.exigirPermiso(Permiso.PROVEEDOR_GESTIONAR);
        if (!repo.existsById(id)) {
            throw new NotFoundException("Proveedor no encontrado");
        }
        if (productoRepo.existsByProveedorId(id) || ordenCompraRepo.existsByProveedorId(id)) {
            throw new ConflictException(
                    "No se puede eliminar el proveedor: tiene productos u ordenes de compra asociadas");
        }
        repo.deleteById(id);
    }
}
