package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ProductoDTO;
import com.stockcomerce.stockapihb.exception.NotFoundException;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements IProductoService {

    @Autowired
    private ProductoRepository repo;

    @Override
    public List<ProductoDTO> mostrarProductos() {
        return repo.findAll().stream().map(Mapper::prodDTO).toList();
    }

    @Override
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {

        var prod = Producto.builder()
                .nombre(productoDTO.getNombre())
                .precio(productoDTO.getPrecio())
                .categoria(productoDTO.getCategoria())
                .build();
        return Mapper.prodDTO(repo.save(prod));
    }

    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        Producto prod = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto No Encontrado"));

        prod.setNombre(productoDTO.getNombre());
        prod.setPrecio(productoDTO.getPrecio());
        prod.setCategoria(productoDTO.getCategoria());

        return Mapper.prodDTO(repo.save(prod));
    }

    @Override
    public void eliminarProducto(Long id) {
        // ojo: estaba invertido, tiraba NotFound cuando el producto SI existia
        if (!repo.existsById(id)) {
            throw new NotFoundException("Producto No Encontrado Para Deletar");
        }

        repo.deleteById(id);
    }
}