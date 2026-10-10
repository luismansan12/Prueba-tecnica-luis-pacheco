package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.dto.PolizaResponse;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import com.pruebatecnica.polizas.model.EstadoPoliza;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.TipoPoliza;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    private static final Long POLIZA_ID = 7L;

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PolizaService polizaService;

    @BeforeEach
    void setUp() {
        polizaService = new PolizaService(polizaRepository, eventPublisher, new BigDecimal("0.10"));
    }

    @Test
    void listarEnviaLosFiltrosAlRepositorio() {
        when(polizaRepository.buscarPorTipoYEstado(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA))
                .thenReturn(List.of(poliza()));

        List<PolizaResponse> resultado = polizaService.listar(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);

        assertThat(resultado).singleElement()
                .satisfies(p -> assertThat(p.id()).isEqualTo(POLIZA_ID));
    }

    @Test
    void renovarUsaElIpcConfiguradoYNotificaElCambio() {
        when(polizaRepository.findById(POLIZA_ID)).thenReturn(Optional.of(poliza()));

        PolizaResponse respuesta = polizaService.renovar(POLIZA_ID);

        assertThat(respuesta.canonMensual()).isEqualByComparingTo("1100000");
        assertThat(respuesta.prima()).isEqualByComparingTo("13200000");
        assertThat(respuesta.estado()).isEqualTo(EstadoPoliza.RENOVADA);
        verify(eventPublisher).publishEvent(new PolizaActualizadaEvent(POLIZA_ID));
    }

    @Test
    void renovarUnaPolizaCanceladaFallaYNoNotifica() {
        Poliza cancelada = poliza();
        cancelada.cancelar();
        when(polizaRepository.findById(POLIZA_ID)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> polizaService.renovar(POLIZA_ID))
                .isInstanceOf(ReglaNegocioException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void cancelarNotificaElCambio() {
        when(polizaRepository.findById(POLIZA_ID)).thenReturn(Optional.of(poliza()));

        PolizaResponse respuesta = polizaService.cancelar(POLIZA_ID);

        assertThat(respuesta.estado()).isEqualTo(EstadoPoliza.CANCELADA);
        verify(eventPublisher).publishEvent(new PolizaActualizadaEvent(POLIZA_ID));
    }

    @Test
    void unaPolizaInexistenteResponde404() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.renovar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verifyNoInteractions(eventPublisher);
    }

    private static Poliza poliza() {
        Poliza poliza = new Poliza("POL-7", TipoPoliza.COLECTIVA, "Inmobiliaria",
                LocalDate.of(2026, 1, 1), 12, new BigDecimal("1000000"));
        ReflectionTestUtils.setField(poliza, "id", POLIZA_ID);
        return poliza;
    }
}
