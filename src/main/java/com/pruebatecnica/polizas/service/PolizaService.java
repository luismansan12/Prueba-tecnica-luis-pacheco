package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.dto.PolizaResponse;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.model.EstadoPoliza;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.TipoPoliza;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BigDecimal ipc;

    public PolizaService(PolizaRepository polizaRepository,
                         ApplicationEventPublisher eventPublisher,
                         @Value("${negocio.ipc}") BigDecimal ipc) {
        this.polizaRepository = polizaRepository;
        this.eventPublisher = eventPublisher;
        this.ipc = ipc;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscarPorTipoYEstado(tipo, estado).stream()
                .map(PolizaResponse::from)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.renovar(ipc);
        eventPublisher.publishEvent(new PolizaActualizadaEvent(poliza.getId()));
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.cancelar();
        eventPublisher.publishEvent(new PolizaActualizadaEvent(poliza.getId()));
        return PolizaResponse.from(poliza);
    }

    private Poliza buscarPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("póliza", polizaId));
    }
}
