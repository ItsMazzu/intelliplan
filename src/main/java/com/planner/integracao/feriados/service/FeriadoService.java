package com.planner.integracao.feriados.service;

import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FeriadoService {

    private static final Logger log = LoggerFactory.getLogger(FeriadoService.class);

    private final BrasilApiClient brasilApiClient;

    public FeriadoService(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
    }

    public List<FeriadoDTO> listarFeriados(int ano) {
        try {
            List<FeriadoDTO> feriados = brasilApiClient.buscarFeriadosPorAno(ano);
            return feriados != null ? feriados : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Falha ao consultar feriados nacionais para o ano {} na BrasilAPI: {}", ano, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
