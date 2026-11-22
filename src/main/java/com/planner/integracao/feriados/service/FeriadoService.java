package com.planner.integracao.feriados.service;

import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeriadoService {

    private static final Logger log = LoggerFactory.getLogger(FeriadoService.class);

    private final BrasilApiClient brasilApiClient;
    private final Map<Integer, List<FeriadoDTO>> cacheFeriados;

    public FeriadoService(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
        this.cacheFeriados = new ConcurrentHashMap<>();
    }

    public List<FeriadoDTO> listarFeriados(int ano) {
        List<FeriadoDTO> emCache = cacheFeriados.get(ano);
        if (emCache != null && !emCache.isEmpty()) {
            return emCache;
        }

        try {
            List<FeriadoDTO> feriados = brasilApiClient.buscarFeriadosPorAno(ano);
            if (feriados != null && !feriados.isEmpty()) {
                cacheFeriados.put(ano, feriados);
                return feriados;
            }
            return Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Falha ao consultar feriados nacionais para o ano {} na BrasilAPI: {}", ano, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
