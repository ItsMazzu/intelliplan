package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.config.setup.InstallationConfigService;
import com.planner.integracao.feriados.dto.FeriadoCategoria;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoIncidenciaDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeriadoServiceTest {

    private final LocalizacaoInstalacaoDTO localizacao = LocalizacaoInstalacaoDTO.vazia();

    @Test
    void deveReutilizarRespostaBemSucedidaPorEscopoEAno() {
        FeriadoProvider provider = provider("national");
        InstallationConfigService config = configuration();
        FeriadoService service = new FeriadoService(List.of(provider), config);
        List<FeriadoDTO> feriados = List.of(feriado(
                LocalDate.of(2026, 1, 1),
                "Confraternização Universal",
                FeriadoCategoria.NACIONAL
        ));
        when(provider.buscar(2026, localizacao)).thenReturn(feriados);

        assertEquals(feriados, service.listarFeriados(2026));
        assertEquals(feriados, service.listarFeriados(2026));

        verify(provider, times(1)).buscar(2026, localizacao);
        verify(provider, never()).buscar(2025, localizacao);
    }

    @Test
    void armazenaRespostaVaziaValidaParaEvitarConsultasRepetidas() {
        FeriadoProvider provider = provider("national");
        FeriadoService service = new FeriadoService(List.of(provider), configuration());
        when(provider.buscar(2026, localizacao)).thenReturn(List.of());

        assertEquals(List.of(), service.listarFeriados(2026));
        assertEquals(List.of(), service.listarFeriados(2026));

        verify(provider, times(1)).buscar(2026, localizacao);
    }

    @Test
    void retornaDadosDosProvedoresDisponiveisSemArmazenarFalhaParcial() {
        FeriadoProvider indisponivel = provider("unavailable");
        FeriadoProvider disponivel = provider("available");
        FeriadoService service = new FeriadoService(List.of(indisponivel, disponivel), configuration());
        FeriadoDTO feriado = feriado(
                LocalDate.of(2026, 1, 1),
                "Confraternização Universal",
                FeriadoCategoria.NACIONAL
        );
        when(indisponivel.buscar(2026, localizacao))
                .thenThrow(new FeriadoProviderException("Falha", new IllegalStateException()))
                .thenReturn(List.of());
        when(disponivel.buscar(2026, localizacao)).thenReturn(List.of(feriado));

        assertEquals(List.of(feriado), service.listarFeriados(2026));
        assertEquals(List.of(feriado), service.listarFeriados(2026));

        verify(indisponivel, times(2)).buscar(2026, localizacao);
        verify(disponivel, times(2)).buscar(2026, localizacao);
    }

    @Test
    void consolidaIncidenciasNoMesmoDiaSemDuplicarFeriado() {
        FeriadoProvider nacional = provider("national");
        FeriadoProvider municipal = provider("municipal");
        FeriadoService service = new FeriadoService(List.of(nacional, municipal), configuration());
        LocalDate data = LocalDate.of(2026, 11, 20);
        when(nacional.buscar(2026, localizacao)).thenReturn(List.of(
                feriado(data, "Dia Nacional de Zumbi e da Consciência Negra", FeriadoCategoria.NACIONAL)));
        when(municipal.buscar(2026, localizacao)).thenReturn(List.of(
                new FeriadoDTO(data, "Dia Nacional de Zumbi e da Consciência Negra", List.of(
                        new FeriadoIncidenciaDTO(
                                FeriadoCategoria.MUNICIPAL, "SP", "3548500", "Prefeitura", "Lei local")))));

        List<FeriadoDTO> resultado = service.listarFeriados(2026);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).incidencias().size());
        assertEquals("Nacional, Municipal", resultado.get(0).descricaoCategorias());
    }

    private FeriadoProvider provider(String id) {
        FeriadoProvider provider = mock(FeriadoProvider.class);
        when(provider.id()).thenReturn(id);
        when(provider.suporta(localizacao)).thenReturn(true);
        return provider;
    }

    private InstallationConfigService configuration() {
        InstallationConfigService config = mock(InstallationConfigService.class);
        when(config.getLocalizacao()).thenReturn(localizacao);
        return config;
    }

    private FeriadoDTO feriado(LocalDate data, String nome, FeriadoCategoria categoria) {
        return new FeriadoDTO(data, nome, List.of(
                new FeriadoIncidenciaDTO(categoria, null, null, "Fonte", "Referência")));
    }
}
