package com.pruebatecnica.polizas.dto;

import jakarta.validation.constraints.NotBlank;

public record AgregarRiesgoRequest(
        @NotBlank String direccionInmueble,
        @NotBlank String arrendatario,
        @NotBlank String arrendador) {
}
