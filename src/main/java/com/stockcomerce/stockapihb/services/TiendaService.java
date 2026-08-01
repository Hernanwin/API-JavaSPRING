package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TiendaService implements ITiendaService {

    @Autowired
    private TiendaRepository repo;

    @Override
    public List<TiendaDTO> mostrarTiendas() {

        return repo.findAll()
                .stream()
                .map(Mapper::tienDTO)
                .toList();
    }

    @Override
    public TiendaDTO crearTienda (TiendaDTO tiendaDTO) {
        Tienda tienda = Tienda.builder()
                .nombre(tiendaDTO.getNombre())
                .direccion(tiendaDTO.getDireccion())
                .build();
        return Mapper.tienDTO(repo.save(tienda));
    }

    @Override
    public TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO) {
        Tienda tienda = repo.findById(id).orElseThrow(() -> new RuntimeException("Tienda no encontrado"));

        tienda.setNombre(tiendaDTO.getNombre());
        tienda.setDireccion(tiendaDTO.getDireccion());

        return Mapper.tienDTO(repo.save(tienda));
    }

    @Override
    public void eliminarTienda(Long id) {
        if (!repo.existsById(id))
            throw new RuntimeException("Tienda no encontrado");

        repo.deleteById(id);
    }
}