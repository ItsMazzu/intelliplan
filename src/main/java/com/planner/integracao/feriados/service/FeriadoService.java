package com.planner.integracao.feriados.service;

import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FeriadoService {

    private static final Logger log = LoggerFactory.getLogger(FeriadoService.class);

    private final BrasilApiClient brasilApiClient;
    private final ConcurrentMap<Integer, List<FeriadoDTO>> feriadosPorAno = new ConcurrentHashMap<>();

    public FeriadoService(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
    }

    public List<FeriadoDTO> listarFeriados(int ano) {
        List<FeriadoDTO> feriados = feriadosPorAno.computeIfAbsent(ano, this::buscarFeriadosNaoVazios);
        return feriados != null ? feriados : Collections.emptyList();
    }

    private List<FeriadoDTO> buscarFeriadosNaoVazios(int ano) {
        try {
            List<FeriadoDTO> feriados = brasilApiClient.buscarFeriadosPorAno(ano);
            if (feriados == null || feriados.isEmpty()) {
                return null;
            }
            return List.copyOf(feriados);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar feriados nacionais para o ano {} na BrasilAPI: {}", ano, ex.getMessage());
            return null;
        }
    }
}
