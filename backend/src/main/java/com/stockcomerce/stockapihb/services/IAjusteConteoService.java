package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.AjusteConteoDTO;

import java.util.List;

public interface IAjusteConteoService {
    List<AjusteConteoDTO> listar(Long idTienda);
    AjusteConteoDTO registrarAjuste(AjusteConteoDTO dto);
}
