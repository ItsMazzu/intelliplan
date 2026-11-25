package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.integracao.feriados.dto.FeriadoCategoria;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoIncidenciaDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SantosMunicipalFeriadoProvider implements FeriadoProvider {

    private static final String CODIGO_IBGE_SANTOS = "3548500";
    private static final String REFERENCIA_LEGAL =
            "Lei Orgânica do Município de Santos (feriados municipais); Decreto Municipal nº 11.111/2025, calendário oficial de 2026";

    @Override
    public String id() {
        return "santos-municipal";
    }

    @Override
    public String descricaoCobertura() {
        return "Santos/SP — feriados municipais previstos na Lei Orgânica e calendário oficial";
    }

    @Override
    public boolean suporta(LocalizacaoInstalacaoDTO localizacao) {
        return localizacao != null
                && "SP".equalsIgnoreCase(localizacao.uf())
                && CODIGO_IBGE_SANTOS.equals(localizacao.codigoIbge());
    }

    @Override
    public List<FeriadoDTO> buscar(int ano, LocalizacaoInstalacaoDTO localizacao) {
        LocalDate pascoa = calcularPascoa(ano);
        List<FeriadoDTO> feriados = new ArrayList<>();
        feriados.add(feriado(LocalDate.of(ano, 1, 26), "Aniversário de Santos"));
        feriados.add(feriado(pascoa.minusDays(2), "Sexta-feira Santa"));
        feriados.add(feriado(pascoa.plusDays(60), "Corpus Christi"));
        feriados.add(feriado(LocalDate.of(ano, 9, 8), "Nossa Senhora do Monte Serrat"));
        return List.copyOf(feriados);
    }

    private FeriadoDTO feriado(LocalDate data, String nome) {
        return new FeriadoDTO(
                data,
                nome,
                List.of(new FeriadoIncidenciaDTO(
                        FeriadoCategoria.MUNICIPAL,
                        "SP",
                        CODIGO_IBGE_SANTOS,
                        "Prefeitura de Santos — Diário Oficial / portal Legis",
                        REFERENCIA_LEGAL
                ))
        );
    }

    private LocalDate calcularPascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
