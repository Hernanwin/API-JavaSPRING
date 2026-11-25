package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.TransferenciaDTO;
import com.stockcomerce.stockapihb.models.EstadoTransferencia;

import java.util.List;

public interface ITransferenciaService {
    List<TransferenciaDTO> mostrarTransferencias();
    List<TransferenciaDTO> listarFiltradas(EstadoTransferencia estado);

    // Ejecucion directa en un paso (requiere capacidad de aprobacion + operacion en origen y destino)
    TransferenciaDTO crearTransferencia(TransferenciaDTO transferenciaDTO);

    // Flujo segregado por riesgo/responsabilidad: Solicitar -> Aprobar -> Enviar -> Recibir
    TransferenciaDTO solicitarTransferencia(TransferenciaDTO transferenciaDTO);
    TransferenciaDTO aprobarTransferencia(Long id);
    TransferenciaDTO rechazarTransferencia(Long id, String observacion);
    TransferenciaDTO enviarTransferencia(Long id);
    TransferenciaDTO recibirTransferencia(Long id, Integer cantidadRecibida, String observacion);
}
