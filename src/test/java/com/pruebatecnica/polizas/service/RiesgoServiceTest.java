package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.dto.AgregarRiesgoRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import com.pruebatecnica.polizas.model.EstadoRiesgo;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.Riesgo;
import com.pruebatecnica.polizas.model.TipoPoliza;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.repository.RiesgoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiesgoServiceTest {

    private static final Long POLIZA_ID = 2L;
    private static final AgregarRiesgoRequest REQUEST =
            new AgregarRiesgoRequest("Cl 10 # 43-12 Apto 901", "Jorge Pérez", "María Restrepo");

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RiesgoService riesgoService;

    @BeforeEach
    void setUp() {
        riesgoService = new RiesgoService(riesgoRepository, polizaRepository, eventPublisher);
    }

    @Test
    void listarRiesgosDeUnaPolizaInexistenteResponde404() {
        when(polizaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> riesgoService.listarPorPoliza(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarDevuelveLosRiesgosDeLaPoliza() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        Riesgo riesgo = new Riesgo("Dirección", "Arrendatario", "Arrendador");
        poliza.agregarRiesgo(riesgo);
        when(polizaRepository.existsById(POLIZA_ID)).thenReturn(true);
        when(riesgoRepository.findByPolizaIdOrderByIdAsc(POLIZA_ID)).thenReturn(List.of(riesgo));

        List<RiesgoResponse> riesgos = riesgoService.listarPorPoliza(POLIZA_ID);

        assertThat(riesgos).singleElement()
                .satisfies(r -> assertThat(r.polizaId()).isEqualTo(POLIZA_ID));
    }

    @Test
    void agregarRiesgoAUnaColectivaLoGuardaYNotifica() {
        when(polizaRepository.findById(POLIZA_ID)).thenReturn(Optional.of(poliza(TipoPoliza.COLECTIVA)));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        RiesgoResponse respuesta = riesgoService.agregar(POLIZA_ID, REQUEST);

        assertThat(respuesta.polizaId()).isEqualTo(POLIZA_ID);
        assertThat(respuesta.estado()).isEqualTo(EstadoRiesgo.ACTIVO);
        assertThat(respuesta.direccionInmueble()).isEqualTo(REQUEST.direccionInmueble());
        verify(eventPublisher).publishEvent(new PolizaActualizadaEvent(POLIZA_ID));
    }

    @Test
    void agregarRiesgoAUnaIndividualFallaSinGuardarNiNotificar() {
        when(polizaRepository.findById(POLIZA_ID)).thenReturn(Optional.of(poliza(TipoPoliza.INDIVIDUAL)));

        assertThatThrownBy(() -> riesgoService.agregar(POLIZA_ID, REQUEST))
                .isInstanceOf(ReglaNegocioException.class);
        verify(riesgoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void agregarRiesgoAUnaPolizaInexistenteResponde404() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riesgoService.agregar(99L, REQUEST))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cancelarRiesgoDeUnaColectivaNotificaElCambioDeSuPoliza() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        Riesgo riesgo = new Riesgo("Dirección", "Arrendatario", "Arrendador");
        poliza.agregarRiesgo(riesgo);
        when(riesgoRepository.findById(10L)).thenReturn(Optional.of(riesgo));

        RiesgoResponse respuesta = riesgoService.cancelar(10L);

        assertThat(respuesta.estado()).isEqualTo(EstadoRiesgo.CANCELADO);
        verify(eventPublisher).publishEvent(new PolizaActualizadaEvent(POLIZA_ID));
    }

    @Test
    void cancelarUnRiesgoInexistenteResponde404() {
        when(riesgoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riesgoService.cancelar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(eventPublisher);
    }

    private static Poliza poliza(TipoPoliza tipo) {
        Poliza poliza = new Poliza("POL-2", tipo, "Tomador", LocalDate.of(2026, 1, 1), 12, new BigDecimal("2500000"));
        ReflectionTestUtils.setField(poliza, "id", POLIZA_ID);
        return poliza;
    }
}
