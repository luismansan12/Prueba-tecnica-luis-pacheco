package com.pruebatecnica.polizas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exige el header api-key en todas las peticiones.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "api-key";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final byte[] apiKeyEsperada;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(@Value("${security.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.apiKeyEsperada = apiKey.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKeyRecibida = request.getHeader(HEADER);
        if (apiKeyRecibida != null && esValida(apiKeyRecibida)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Petición rechazada por api-key ausente o inválida: {} {}", request.getMethod(), request.getRequestURI());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "El header api-key es obligatorio y debe ser válido");
        problema.setTitle("No autorizado");
        problema.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), problema);
    }

    private boolean esValida(String apiKeyRecibida) {
        // Comparación en tiempo constante para no dar pistas por tiempos de respuesta.
        return MessageDigest.isEqual(apiKeyEsperada, apiKeyRecibida.getBytes(StandardCharsets.UTF_8));
    }
}
