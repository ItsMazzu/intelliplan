package com.planner.config.localidade;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
public class IbgeLocalidadeClient {

    private final RestClient restClient;

    public IbgeLocalidadeClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        restClient = RestClient.builder()
                .baseUrl("https://servicodados.ibge.gov.br/api/v1/localidades")
                .requestFactory(requestFactory)
                .build();
    }

    public List<EstadoDTO> listarEstados() {
        List<EstadoResponse> estados = restClient.get()
                .uri("/estados?orderBy=nome")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (estados == null) {
            throw new RestClientException("A fonte de localidades retornou uma resposta inválida.");
        }
        return estados.stream().map(estado -> new EstadoDTO(estado.sigla(), estado.nome())).toList();
    }

    public List<MunicipioDTO> listarMunicipios(String uf) {
        List<JsonNode> municipios = restClient.get()
                .uri("/estados/{uf}/municipios?orderBy=nome", uf)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (municipios == null) {
            throw new RestClientException("A fonte de localidades retornou uma resposta inválida.");
        }
        return municipios.stream()
                .map(municipio -> new MunicipioDTO(
                        municipio.path("id").asText(),
                        municipio.path("nome").asText(),
                        obterUf(municipio)))
                .toList();
    }

    private String obterUf(JsonNode municipio) {
        JsonNode uf = municipio.path("regiao-imediata")
                .path("regiao-intermediaria")
                .path("UF");
        if (uf.isMissingNode()) {
            uf = municipio.path("microrregiao").path("mesorregiao").path("UF");
        }
        return uf.path("sigla").asText();
    }

    private record EstadoResponse(int id, String sigla, String nome) {
    }
}
