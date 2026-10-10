package com.pruebatecnica.polizas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP hacia el servicio de edición del CORE, con tiempos máximos de espera
 * para que una demora del CORE no deje bloqueadas las peticiones.
 */
@Configuration
public class CoreClientConfig {

    @Bean
    public RestClient coreRestClient(RestClient.Builder restClientBuilder,
                                     @Value("${core.base-url}") String baseUrl,
                                     @Value("${core.connect-timeout}") Duration connectTimeout,
                                     @Value("${core.read-timeout}") Duration readTimeout,
                                     @Value("${security.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(ApiKeyFilter.HEADER, apiKey)
                .build();
    }
}
