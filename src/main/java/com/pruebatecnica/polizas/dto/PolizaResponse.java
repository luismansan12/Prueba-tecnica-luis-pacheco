package com.pruebatecnica.polizas.dto;

import com.pruebatecnica.polizas.model.EstadoPoliza;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        String numero,
        TipoPoliza tipo,
        EstadoPoliza estado,
        String tomador,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int mesesVigencia,
        BigDecimal canonMensual,
        BigDecimal prima) {

    public static PolizaResponse from(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getNumero(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getTomador(),
                poliza.getFechaInicio(),
                poliza.getFechaFin(),
                poliza.getMesesVigencia(),
                poliza.getCanonMensual(),
                poliza.getPrima());
    }
}
