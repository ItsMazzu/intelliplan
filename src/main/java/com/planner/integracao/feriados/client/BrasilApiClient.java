package com.planner.integracao.feriados.client;

import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
public class BrasilApiClient {

    private final RestClient restClient;

    public BrasilApiClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl("https://brasilapi.com.br")
                .requestFactory(requestFactory)
                .build();
    }

    public List<FeriadoDTO> buscarFeriadosPorAno(int ano) {
        return restClient.get()
                .uri("/api/feriados/v1/{ano}", ano)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FeriadoDTO>>() {});
    }
}
