package com.planner.config.localidade;

import com.planner.common.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LocalidadeService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final IbgeLocalidadeClient ibgeLocalidadeClient;
    private final ConcurrentMap<String, CacheEntry<MunicipioDTO>> municipiosPorUf = new ConcurrentHashMap<>();
    private volatile CacheEntry<EstadoDTO> estados;

    public LocalidadeService(IbgeLocalidadeClient ibgeLocalidadeClient) {
        this.ibgeLocalidadeClient = ibgeLocalidadeClient;
    }

    public List<EstadoDTO> listarEstados() {
        CacheEntry<EstadoDTO> resultado = estados;
        if (resultado != null && resultado.expiraEm().isAfter(Instant.now())) {
            return resultado.valores();
        }
        synchronized (this) {
            resultado = estados;
            if (resultado == null || !resultado.expiraEm().isAfter(Instant.now())) {
                estados = new CacheEntry<>(List.copyOf(ibgeLocalidadeClient.listarEstados()),
                        Instant.now().plus(CACHE_TTL));
            }
            return estados.valores();
        }
    }

    public List<MunicipioDTO> listarMunicipios(String uf) {
        String ufNormalizada = normalizarUf(uf);
        if (!ufNormalizada.matches("[A-Z]{2}")) {
            throw new BusinessRuleException("A UF informada é inválida.");
        }
        CacheEntry<MunicipioDTO> resultado = municipiosPorUf.get(ufNormalizada);
        if (resultado != null && resultado.expiraEm().isAfter(Instant.now())) {
            return resultado.valores();
        }
        synchronized (municipiosPorUf) {
            resultado = municipiosPorUf.get(ufNormalizada);
            if (resultado == null || !resultado.expiraEm().isAfter(Instant.now())) {
                resultado = new CacheEntry<>(
                        List.copyOf(ibgeLocalidadeClient.listarMunicipios(ufNormalizada)),
                        Instant.now().plus(CACHE_TTL));
                municipiosPorUf.put(ufNormalizada, resultado);
            }
            return resultado.valores();
        }
    }

    public LocalizacaoInstalacaoDTO validar(LocalizacaoRequestDTO localizacao) {
        String uf = normalizarUf(localizacao.uf());
        String codigoIbge = localizacao.codigoIbge().trim();
        String nomeMunicipio = localizacao.municipio().trim();
        boolean estadoValido = listarEstados().stream()
                .anyMatch(estado -> estado.sigla().equals(uf));
        if (!estadoValido) {
            throw new BusinessRuleException("Selecione um estado válido.");
        }

        MunicipioDTO municipio = listarMunicipios(uf).stream()
                .filter(item -> item.codigoIbge().equals(codigoIbge))
                .filter(item -> item.uf().equals(uf))
                .filter(item -> normalizarNome(item.nome()).equals(normalizarNome(nomeMunicipio)))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "O município não corresponde ao estado e ao código IBGE informados."));
        return new LocalizacaoInstalacaoDTO(municipio.uf(), municipio.nome(), municipio.codigoIbge());
    }

    private String normalizarUf(String uf) {
        return uf == null ? "" : uf.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarNome(String nome) {
        return nome.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record CacheEntry<T>(List<T> valores, Instant expiraEm) {
        private CacheEntry {
            valores = List.copyOf(valores);
        }
    }
}

