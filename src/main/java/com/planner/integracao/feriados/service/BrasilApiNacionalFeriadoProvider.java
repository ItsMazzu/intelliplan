package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.BrasilApiFeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoCategoria;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoIncidenciaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class BrasilApiNacionalFeriadoProvider implements FeriadoProvider {

    private static final Map<MonthDay, Holiday> FERIADOS_NACIONAIS = Map.of(
            MonthDay.of(1, 1), new Holiday("Confraternização Universal", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(4, 21), new Holiday("Tiradentes", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(5, 1), new Holiday("Dia Mundial do Trabalho", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(9, 7), new Holiday("Independência do Brasil", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(10, 12), new Holiday("Nossa Senhora Aparecida", "Lei Federal nº 6.802/1980, art. 1º"),
            MonthDay.of(11, 2), new Holiday("Finados", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(11, 15), new Holiday("Proclamação da República", "Lei Federal nº 662/1949, art. 1º"),
            MonthDay.of(11, 20), new Holiday("Dia Nacional de Zumbi e da Consciência Negra",
                    "Lei Federal nº 14.759/2023, art. 1º"),
            MonthDay.of(12, 25), new Holiday("Natal", "Lei Federal nº 662/1949, art. 1º")
    );
    private static final Map<Integer, LocalDate> QUARTA_FEIRA_DE_CINZAS = Map.of(
            2026, LocalDate.of(2026, 2, 18)
    );

    private final BrasilApiClient brasilApiClient;

    public BrasilApiNacionalFeriadoProvider(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
    }

    @Override
    public String id() {
        return "brasilapi-national";
    }

    @Override
    public String descricaoCobertura() {
        return "BrasilAPI — feriados nacionais legais e datas facultativas em classificação separada";
    }

    @Override
    public boolean suporta(LocalizacaoInstalacaoDTO localizacao) {
        return true;
    }

    @Override
    public List<FeriadoDTO> buscar(int ano, LocalizacaoInstalacaoDTO localizacao) {
        final List<BrasilApiFeriadoDTO> resposta;
        try {
            resposta = brasilApiClient.buscarFeriadosPorAno(ano);
        } catch (RestClientException ex) {
            throw new FeriadoProviderException("Falha ao consultar a BrasilAPI.", ex);
        }
        if (resposta == null) {
            throw new FeriadoProviderException("A BrasilAPI retornou uma resposta vazia.");
        }
        List<FeriadoDTO> feriados = resposta.stream()
                .filter(feriado -> feriado != null && feriado.date() != null && feriado.date().getYear() == ano)
                .map(feriado -> {
                    Holiday holiday = FERIADOS_NACIONAIS.get(MonthDay.from(feriado.date()));
                    if (holiday != null) {
                        return criarFeriado(feriado.date(), holiday.nome(), FeriadoCategoria.NACIONAL,
                                "BrasilAPI — https://brasilapi.com.br/api/feriados/v1/" + ano,
                                holiday.referenciaLegal());
                    }
                    String nomePontoFacultativo = nomePontoFacultativo(feriado.name());
                    return nomePontoFacultativo == null ? null : criarFeriado(
                            feriado.date(),
                            nomePontoFacultativo,
                            FeriadoCategoria.PONTO_FACULTATIVO,
                            "BrasilAPI — https://brasilapi.com.br/api/feriados/v1/" + ano,
                            "Calendário federal de feriados e pontos facultativos do ano " + ano
                    );
                })
                .filter(feriado -> feriado != null)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        LocalDate quartaCinzas = QUARTA_FEIRA_DE_CINZAS.get(ano);
        if (quartaCinzas != null && feriados.stream().noneMatch(feriado -> feriado.date().equals(quartaCinzas))) {
            feriados.add(criarFeriado(
                    quartaCinzas,
                    "Quarta-feira de Cinzas (até 14h)",
                    FeriadoCategoria.PONTO_FACULTATIVO,
                    "Ministério da Gestão e da Inovação em Serviços Públicos",
                    "Calendário oficial de feriados e pontos facultativos de 2026"
            ));
        }
        return List.copyOf(feriados);
    }

    private FeriadoDTO criarFeriado(LocalDate data,
                                    String nome,
                                    FeriadoCategoria categoria,
                                    String fonte,
                                    String referenciaLegal) {
        return new FeriadoDTO(
                data,
                nome,
                List.of(new FeriadoIncidenciaDTO(categoria, null, null, fonte, referenciaLegal))
        );
    }

    private String nomePontoFacultativo(String nome) {
        if (nome == null) {
            return null;
        }
        return switch (nome.trim().toLowerCase(Locale.ROOT)) {
            case "carnaval" -> "Carnaval";
            case "quarta-feira de cinzas", "quarta feira de cinzas" -> "Quarta-feira de Cinzas (até 14h)";
            case "sexta-feira santa", "sexta feira santa" -> "Sexta-feira Santa";
            case "corpus christi" -> "Corpus Christi";
            default -> null;
        };
    }

    private record Holiday(String nome, String referenciaLegal) {
    }
}
