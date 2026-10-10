package com.pruebatecnica.polizas.model;

import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolizaTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final BigDecimal IPC = new BigDecimal("0.0520");

    @Test
    void alCrearUnaPolizaLaPrimaEsCanonPorMesesYQuedaActiva() {
        Poliza poliza = colectiva();

        assertThat(poliza.getPrima()).isEqualByComparingTo("18000000");
        assertThat(poliza.getFechaFin()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.ACTIVA);
    }

    @Test
    void renovarAjustaCanonYPrimaConElIpcYCorreLaVigencia() {
        Poliza poliza = colectiva();

        poliza.renovar(IPC);

        // 1.500.000 x 1,052 = 1.578.000 ; prima = 1.578.000 x 12
        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1578000");
        assertThat(poliza.getPrima()).isEqualByComparingTo("18936000");
        assertThat(poliza.getFechaInicio()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getFechaFin()).isEqualTo(LocalDate.of(2028, 1, 1));
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
    }

    @Test
    void renovarRedondeaElCanonAPesos() {
        Poliza poliza = new Poliza("POL-1", TipoPoliza.INDIVIDUAL, "Ana", INICIO, 6, new BigDecimal("1234567"));

        poliza.renovar(IPC);

        // 1.234.567 x 1,052 = 1.298.764,484 -> 1.298.764
        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1298764");
        assertThat(poliza.getPrima()).isEqualByComparingTo("7792584");
    }

    @Test
    void unaPolizaRenovadaSePuedeVolverARenovar() {
        Poliza poliza = colectiva();
        poliza.renovar(IPC);

        poliza.renovar(IPC);

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(poliza.getFechaFin()).isEqualTo(LocalDate.of(2029, 1, 1));
    }

    @Test
    void noSePuedeRenovarUnaPolizaCancelada() {
        Poliza poliza = colectiva();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.renovar(IPC))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");
        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1500000");
    }

    @Test
    void cancelarLaPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = colectiva();
        Riesgo primero = riesgo();
        Riesgo segundo = riesgo();
        poliza.agregarRiesgo(primero);
        poliza.agregarRiesgo(segundo);

        poliza.cancelar();

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(poliza.getRiesgos()).extracting(Riesgo::getEstado).containsOnly(EstadoRiesgo.CANCELADO);
    }

    @Test
    void cancelarUnaColectivaConUnRiesgoYaCanceladoNoFalla() {
        Poliza poliza = colectiva();
        Riesgo cancelado = riesgo();
        poliza.agregarRiesgo(cancelado);
        poliza.agregarRiesgo(riesgo());
        cancelado.cancelar();

        poliza.cancelar();

        assertThat(poliza.getRiesgos()).extracting(Riesgo::getEstado).containsOnly(EstadoRiesgo.CANCELADO);
    }

    @Test
    void noSePuedeCancelarDosVecesLaMismaPoliza() {
        Poliza poliza = colectiva();
        poliza.cancelar();

        assertThatThrownBy(poliza::cancelar)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está cancelada");
    }

    @Test
    void aUnaPolizaIndividualNoSeLePuedenAgregarRiesgos() {
        Poliza individual = new Poliza("POL-1", TipoPoliza.INDIVIDUAL, "Ana", INICIO, 12, new BigDecimal("1500000"));
        individual.vincularRiesgo(riesgo());

        assertThatThrownBy(() -> individual.agregarRiesgo(riesgo()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("colectivas");
        assertThat(individual.getRiesgos()).hasSize(1);
    }

    @Test
    void noSePuedenAgregarRiesgosAUnaColectivaCancelada() {
        Poliza poliza = colectiva();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.agregarRiesgo(riesgo()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void agregarRiesgoAUnaColectivaLoDejaActivoYVinculado() {
        Poliza poliza = colectiva();
        Riesgo riesgo = riesgo();

        poliza.agregarRiesgo(riesgo);

        assertThat(poliza.getRiesgos()).containsExactly(riesgo);
        assertThat(riesgo.getPoliza()).isSameAs(poliza);
        assertThat(riesgo.getEstado()).isEqualTo(EstadoRiesgo.ACTIVO);
    }

    private static Poliza colectiva() {
        return new Poliza("POL-2", TipoPoliza.COLECTIVA, "Inmobiliaria Los Andes", INICIO, 12, new BigDecimal("1500000"));
    }

    private static Riesgo riesgo() {
        return new Riesgo("Cra 15 # 93-20", "Arrendatario", "Arrendador");
    }
}
