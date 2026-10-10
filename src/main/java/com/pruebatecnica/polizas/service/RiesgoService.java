package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.dto.AgregarRiesgoRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.Riesgo;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.repository.RiesgoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final PolizaRepository polizaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RiesgoService(RiesgoRepository riesgoRepository,
                         PolizaRepository polizaRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.riesgoRepository = riesgoRepository;
        this.polizaRepository = polizaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarPorPoliza(Long polizaId) {
        if (!polizaRepository.existsById(polizaId)) {
            throw new RecursoNoEncontradoException("póliza", polizaId);
        }
        return riesgoRepository.findByPolizaIdOrderByIdAsc(polizaId).stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public RiesgoResponse agregar(Long polizaId, AgregarRiesgoRequest request) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("póliza", polizaId));

        Riesgo riesgo = new Riesgo(request.direccionInmueble(), request.arrendatario(), request.arrendador());
        poliza.agregarRiesgo(riesgo);
        Riesgo guardado = riesgoRepository.save(riesgo);

        eventPublisher.publishEvent(new PolizaActualizadaEvent(poliza.getId()));
        return RiesgoResponse.from(guardado);
    }

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("riesgo", riesgoId));

        riesgo.cancelar();

        eventPublisher.publishEvent(new PolizaActualizadaEvent(riesgo.getPoliza().getId()));
        return RiesgoResponse.from(riesgo);
    }
}
