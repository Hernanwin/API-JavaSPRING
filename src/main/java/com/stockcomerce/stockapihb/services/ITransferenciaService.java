package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;

import java.util.List;

public interface ITransferenciaService {
    List<TransferenciaDTO> mostrarTransferencias();
    TransferenciaDTO crearTransferencia(TransferenciaDTO transferenciaDTO);
}
