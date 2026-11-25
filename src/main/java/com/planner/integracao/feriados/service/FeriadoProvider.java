package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.integracao.feriados.dto.FeriadoDTO;

import java.util.List;

public interface FeriadoProvider {

    String id();

    String descricaoCobertura();

    boolean suporta(LocalizacaoInstalacaoDTO localizacao);

    List<FeriadoDTO> buscar(int ano, LocalizacaoInstalacaoDTO localizacao);
}
