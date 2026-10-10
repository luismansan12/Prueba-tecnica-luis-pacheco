package com.pruebatecnica.polizas.controller;

import com.pruebatecnica.polizas.dto.CoreEventoRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simula el servicio de edición del CORE: solo deja en el log que la operación se intentó enviar.
 */
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public ResponseEntity<Void> registrarEvento(@Valid @RequestBody CoreEventoRequest request) {
        log.info("[CORE-MOCK] Se intentó enviar al CORE el evento {} de la póliza {}",
                request.evento(), request.polizaId());
        return ResponseEntity.accepted().build();
    }
}
