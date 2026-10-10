package com.pruebatecnica.polizas.model;

import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiesgoTest {

    @Test
    void cancelarUnRiesgoActivoDeUnaColectiva() {
        Riesgo riesgo = riesgoDe(TipoPoliza.COLECTIVA);

        riesgo.cancelar();

        assertThat(riesgo.getEstado()).isEqualTo(EstadoRiesgo.CANCELADO);
        assertThat(riesgo.estaActivo()).isFalse();
    }

    @Test
    void noSePuedeCancelarUnRiesgoYaCancelado() {
        Riesgo riesgo = riesgoDe(TipoPoliza.COLECTIVA);
        riesgo.cancelar();

        assertThatThrownBy(riesgo::cancelar)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está cancelado");
    }

    @Test
    void elRiesgoDeUnaPolizaIndividualNoSeCancelaPorSeparado() {
        Riesgo riesgo = riesgoDe(TipoPoliza.INDIVIDUAL);

        assertThatThrownBy(riesgo::cancelar)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("se debe cancelar la póliza");
        assertThat(riesgo.getEstado()).isEqualTo(EstadoRiesgo.ACTIVO);
    }

    private static Riesgo riesgoDe(TipoPoliza tipo) {
        Poliza poliza = new Poliza("POL-1", tipo, "Tomador", LocalDate.of(2026, 1, 1), 12, new BigDecimal("1000000"));
        Riesgo riesgo = new Riesgo("Cl 10 # 43-12", "Arrendatario", "Arrendador");
        poliza.vincularRiesgo(riesgo);
        return riesgo;
    }
}
