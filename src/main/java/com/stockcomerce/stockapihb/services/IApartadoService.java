package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.ApartadoDTO;
import com.stockcomerce.stockapihb.models.EstadoApartado;

import java.util.List;

public interface IApartadoService {
    List<ApartadoDTO> listar(Long idTienda, EstadoApartado estado);
    ApartadoDTO crear(ApartadoDTO dto);
    ApartadoDTO cobrar(Long id);
    ApartadoDTO cancelar(Long id);
}
