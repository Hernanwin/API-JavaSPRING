package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.EntradaDTO;

import java.util.List;

public interface IEntradaService {
    List<EntradaDTO> mostrarEntradas();
    EntradaDTO crearEntrada(EntradaDTO entradaDTO);
}
