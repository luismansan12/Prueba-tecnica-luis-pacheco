package com.pruebatecnica.polizas.controller;

import com.pruebatecnica.polizas.dto.AgregarRiesgoRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.service.RiesgoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    @GetMapping("/polizas/{polizaId}/riesgos")
    public List<RiesgoResponse> listarPorPoliza(@PathVariable Long polizaId) {
        return riesgoService.listarPorPoliza(polizaId);
    }

    @PostMapping("/polizas/{polizaId}/riesgos")
    @ResponseStatus(HttpStatus.CREATED)
    public RiesgoResponse agregar(@PathVariable Long polizaId,
                                  @Valid @RequestBody AgregarRiesgoRequest request) {
        return riesgoService.agregar(polizaId, request);
    }

    @PostMapping("/riesgos/{id}/cancelar")
    public RiesgoResponse cancelar(@PathVariable Long id) {
        return riesgoService.cancelar(id);
    }
}
