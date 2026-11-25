package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.exception.ConflictException;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Proveedor;
import com.stockcomerce.stockapihb.repositories.*;
import com.stockcomerce.stockapihb.security.AccessControlService;
import com.stockcomerce.stockapihb.security.Permiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductoService implements IProductoService {

    @Autowired
    private ProductoRepository repo;
    @Autowired
    private ProveedorRepository proveedorRepo;
    @Autowired
    private InventarioRepository inventarioRepo;
    @Autowired
    private EntradaRepository entradaRepo;
    @Autowired
    private SalidaRepository salidaRepo;
    @Autowired
    private TransferenciaRepository transferenciaRepo;
    @Autowired
    private DetalleVentaRepository detalleVentaRepo;
    @Autowired
    private AjusteConteoRepository ajusteConteoRepo;
    @Autowired
    private AccessControlService accessControl;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> mostrarProductos() {
        accessControl.exigirPermiso(Permiso.CATALOGO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findAll().stream()
                .map(p -> Mapper.prodDTO(p, verCostos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> mostrarProductosPorProveedor(Long idProveedor) {
        accessControl.exigirPermiso(Permiso.CATALOGO_VER);
        boolean verCostos = accessControl.puedeVerCostos();
        return repo.findByProveedorId(idProveedor).stream()
                .map(p -> Mapper.prodDTO(p, verCostos))
                .toList();
    }

    @Override
    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        accessControl.exigirPermiso(Permiso.CATALOGO_GESTIONAR);
        accessControl.exigirPermiso(Permiso.PRECIOS_MODIFICAR);
        validar(productoDTO);

        Proveedor proveedor = resolverProveedor(productoDTO.getIdProveedor());
        var prod = Producto.builder()
                .nombre(productoDTO.getNombre())
                .precio(productoDTO.getPrecio())
                .costo(productoDTO.getCosto())
                .categoria(productoDTO.getCategoria())
                .proveedor(proveedor)
                .build();
        return Mapper.prodDTO(repo.save(prod), accessControl.puedeVerCostos());
    }

    @Override
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        accessControl.exigirPermiso(Permiso.CATALOGO_GESTIONAR);
        Producto prod = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto No Encontrado"));
        validar(productoDTO);

        boolean cambiaPrecio = !compararMonto(prod.getPrecio(), productoDTO.getPrecio());
        boolean cambiaCosto = productoDTO.getCosto() != null
                && !compararMonto(prod.getCosto(), productoDTO.getCosto());
        if (cambiaPrecio || cambiaCosto) {
            accessControl.exigirPermiso(Permiso.PRECIOS_MODIFICAR);
        }

        prod.setNombre(productoDTO.getNombre());
        prod.setPrecio(productoDTO.getPrecio());
        if (productoDTO.getCosto() != null) {
            prod.setCosto(productoDTO.getCosto());
        }
        prod.setCategoria(productoDTO.getCategoria());
        if (productoDTO.getIdProveedor() != null) {
            prod.setProveedor(resolverProveedor(productoDTO.getIdProveedor()));
        }
        return Mapper.prodDTO(repo.save(prod), accessControl.puedeVerCostos());
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        accessControl.exigirPermiso(Permiso.CATALOGO_GESTIONAR);
        accessControl.exigirPermiso(Permiso.PRECIOS_MODIFICAR);
        if (!repo.existsById(id)) {
            throw new NotFoundException("Producto No Encontrado Para Deletar");
        }
        if (inventarioRepo.existsByProductoId(id) || entradaRepo.existsByProductoId(id)
                || salidaRepo.existsByProductoId(id) || transferenciaRepo.existsByProductoId(id)
                || detalleVentaRepo.existsByProductoId(id) || ajusteConteoRepo.existsByProductoId(id)) {
            throw new ConflictException(
                    "No se puede eliminar el producto: tiene inventario, movimientos o ventas asociadas");
        }
        repo.deleteById(id);
    }

    private Proveedor resolverProveedor(Long idProveedor) {
        if (idProveedor == null) return null;
        return proveedorRepo.findById(idProveedor)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + idProveedor));
    }

    private static boolean compararMonto(BigDecimal a, BigDecimal b) {
        if (Objects.equals(a, b)) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private void validar(ProductoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ProductoDTO no existe");
        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio es obligatorio y no puede ser negativo");
        if (dto.getCosto() != null && dto.getCosto().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El costo no puede ser negativo");
    }
}
