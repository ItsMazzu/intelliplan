package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.integracao.feriados.dto.FeriadoCategoria;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoIncidenciaDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SaoPauloEstadualFeriadoProvider implements FeriadoProvider {

    @Override
    public String id() {
        return "sao-paulo-state";
    }

    @Override
    public String descricaoCobertura() {
        return "Estado de São Paulo — 9 de julho (Lei Estadual nº 9.497/1997)";
    }

    @Override
    public boolean suporta(LocalizacaoInstalacaoDTO localizacao) {
        return localizacao != null && "SP".equalsIgnoreCase(localizacao.uf());
    }

    @Override
    public List<FeriadoDTO> buscar(int ano, LocalizacaoInstalacaoDTO localizacao) {
        LocalDate data = LocalDate.of(ano, 7, 9);
        return List.of(new FeriadoDTO(
                data,
                "Revolução Constitucionalista de 1932",
                List.of(new FeriadoIncidenciaDTO(
                        FeriadoCategoria.ESTADUAL,
                        "SP",
                        null,
                        "Assembleia Legislativa do Estado de São Paulo — https://www.al.sp.gov.br/repositorio/legislacao/lei/1997/lei-9497-05.03.1997.html",
                        "Lei Estadual nº 9.497/1997, art. 1º"
                ))
        ));
    }
}
