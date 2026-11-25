package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.config.setup.InstallationConfigService;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FeriadoService {

    private static final Logger log = LoggerFactory.getLogger(FeriadoService.class);
    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final List<FeriadoProvider> providers;
    private final InstallationConfigService installationConfigService;
    private final ConcurrentMap<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();

    public FeriadoService(List<FeriadoProvider> providers,
                          InstallationConfigService installationConfigService) {
        this.providers = providers.stream()
                .sorted(Comparator.comparing(FeriadoProvider::id))
                .toList();
        this.installationConfigService = installationConfigService;
    }

    public List<FeriadoDTO> listarFeriados(int ano) {
        LocalizacaoInstalacaoDTO localizacao = installationConfigService.getLocalizacao();
        String uf = localizacao.configurada() ? localizacao.uf() : "";
        String codigoIbge = localizacao.configurada() ? localizacao.codigoIbge() : "";
        String escopo = providers.stream()
                .filter(provider -> provider.suporta(localizacao))
                .map(FeriadoProvider::id)
                .reduce((primeiro, segundo) -> primeiro + "," + segundo)
                .orElse("");
        CacheKey key = new CacheKey(ano, uf, codigoIbge, escopo);

        CacheEntry existente = cache.get(key);
        if (existente != null && existente.expiraEm().isAfter(Instant.now())) {
            return existente.feriados();
        }
        synchronized (cache) {
            existente = cache.get(key);
            if (existente != null && existente.expiraEm().isAfter(Instant.now())) {
                return existente.feriados();
            }
            if (existente != null) {
                cache.remove(key, existente);
            }
            FetchResult resultado = buscar(ano, localizacao);
            if (resultado.cacheavel()) {
                cache.put(key, new CacheEntry(resultado.feriados(), Instant.now().plus(CACHE_TTL)));
            }
            return resultado.feriados();
        }
    }

    public List<String> coberturaLocalAtual() {
        LocalizacaoInstalacaoDTO localizacao = installationConfigService.getLocalizacao();
        return providers.stream()
                .filter(provider -> provider.suporta(localizacao))
                .filter(provider -> !"brasilapi-national".equals(provider.id()))
                .map(FeriadoProvider::descricaoCobertura)
                .toList();
    }

    public void invalidarCache() {
        cache.clear();
    }

    private FetchResult buscar(int ano, LocalizacaoInstalacaoDTO localizacao) {
        List<FeriadoDTO> feriados = new ArrayList<>();
        boolean falhaEmProvedor = false;
        for (FeriadoProvider provider : providers) {
            if (!provider.suporta(localizacao)) {
                continue;
            }
            try {
                List<FeriadoDTO> encontrados = provider.buscar(ano, localizacao);
                if (encontrados == null) {
                    throw new FeriadoProviderException("O provedor retornou uma resposta inválida.");
                }
                feriados.addAll(encontrados);
            } catch (FeriadoProviderException ex) {
                log.warn("Provedor de feriados {} indisponível para o ano {}.", provider.id(), ano);
                falhaEmProvedor = true;
            }
        }
        List<FeriadoDTO> consolidados = consolidar(feriados);
        return new FetchResult(consolidados, !falhaEmProvedor);
    }

    private List<FeriadoDTO> consolidar(List<FeriadoDTO> feriados) {
        Map<EventKey, List<FeriadoDTO>> eventos = new LinkedHashMap<>();
        for (FeriadoDTO feriado : feriados) {
            if (feriado == null || feriado.date() == null || feriado.name() == null || feriado.name().isBlank()) {
                continue;
            }
            EventKey key = new EventKey(feriado.date());
            eventos.computeIfAbsent(key, ignored -> new ArrayList<>()).add(feriado);
        }
        return eventos.values().stream()
                .map(this::mesclarIncidencias)
                .sorted(Comparator.comparing(FeriadoDTO::date).thenComparing(FeriadoDTO::name))
                .toList();
    }

    private FeriadoDTO mesclarIncidencias(List<FeriadoDTO> eventos) {
        FeriadoDTO principal = eventos.get(0);
        List<com.planner.integracao.feriados.dto.FeriadoIncidenciaDTO> incidencias = eventos.stream()
                .flatMap(evento -> evento.incidencias().stream())
                .distinct()
                .sorted(Comparator.comparing(incidencia -> incidencia.categoria()))
                .toList();
        String nomes = eventos.stream()
                .map(FeriadoDTO::name)
                .distinct()
                .reduce((primeiro, segundo) -> primeiro + " / " + segundo)
                .orElse(principal.name());
        return new FeriadoDTO(principal.date(), nomes, incidencias);
    }

    private record CacheKey(int ano, String uf, String codigoIbge, String escopo) {
    }

    private record CacheEntry(List<FeriadoDTO> feriados, Instant expiraEm) {
        private CacheEntry {
            feriados = List.copyOf(feriados);
        }
    }

    private record FetchResult(List<FeriadoDTO> feriados, boolean cacheavel) {
        private FetchResult {
            feriados = List.copyOf(feriados);
        }
    }

    private record EventKey(java.time.LocalDate data) {
    }
}
