package com.planner.integracao.feriados.service;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.config.setup.InstallationConfigService;
import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.BrasilApiFeriadoDTO;
import com.planner.integracao.feriados.dto.FeriadoCategoria;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeriadoProvidersTest {

    @Test
    void preservaDatasFacultativasSemClassificaLasComoFeriadosNacionais() {
        BrasilApiClient client = mock(BrasilApiClient.class);
        when(client.buscarFeriadosPorAno(2026)).thenReturn(List.of(
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 1, 1), "Ano Novo", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 2, 16), "Carnaval", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 2, 17), "Carnaval", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 4, 3), "Sexta-feira Santa", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 4, 5), "Páscoa", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 6, 4), "Corpus Christi", "national"),
                new BrasilApiFeriadoDTO(LocalDate.of(2026, 11, 20), "Consciência Negra", "national")
        ));
        BrasilApiNacionalFeriadoProvider provider = new BrasilApiNacionalFeriadoProvider(client);

        var resultado = provider.buscar(2026, LocalizacaoInstalacaoDTO.vazia());

        assertEquals(7, resultado.size());
        assertEquals(FeriadoCategoria.NACIONAL, resultado.stream()
                .filter(feriado -> feriado.date().equals(LocalDate.of(2026, 1, 1)))
                .findFirst().orElseThrow().incidencias().get(0).categoria());
        assertEquals(FeriadoCategoria.NACIONAL, resultado.stream()
                .filter(feriado -> feriado.date().equals(LocalDate.of(2026, 11, 20)))
                .findFirst().orElseThrow().incidencias().get(0).categoria());
        assertTrue(resultado.stream()
                .filter(feriado -> feriado.name().equals("Carnaval"))
                .allMatch(feriado -> feriado.incidencias().get(0).categoria()
                        == FeriadoCategoria.PONTO_FACULTATIVO));
        var quartaCinzas = resultado.stream()
                .filter(feriado -> feriado.date().equals(LocalDate.of(2026, 2, 18)))
                .findFirst().orElseThrow();
        assertEquals("Quarta-feira de Cinzas (até 14h)", quartaCinzas.name());
        assertTrue(quartaCinzas.somentePontoFacultativo());
        assertFalse(resultado.stream().anyMatch(feriado -> feriado.date().equals(LocalDate.of(2026, 4, 5))));
    }

    @Test
    void feriadoEstadualDeSaoPauloSoAplicaParaUfCorrespondente() {
        SaoPauloEstadualFeriadoProvider provider = new SaoPauloEstadualFeriadoProvider();

        assertTrue(provider.suporta(new LocalizacaoInstalacaoDTO("SP", "Santos", "3548500")));
        assertFalse(provider.suporta(new LocalizacaoInstalacaoDTO("RJ", "Niterói", "3303302")));
        assertEquals(LocalDate.of(2026, 7, 9), provider.buscar(
                2026, new LocalizacaoInstalacaoDTO("SP", "Santos", "3548500")).get(0).date());
    }

    @Test
    void feriadosMunicipaisDeSantosRespeitamLocalidadeECalculamDatasMoveis() {
        SantosMunicipalFeriadoProvider provider = new SantosMunicipalFeriadoProvider();
        LocalizacaoInstalacaoDTO santos = new LocalizacaoInstalacaoDTO("SP", "Santos", "3548500");

        assertTrue(provider.suporta(santos));
        assertFalse(provider.suporta(new LocalizacaoInstalacaoDTO("SP", "São Paulo", "3550308")));
        var resultado = provider.buscar(2026, santos);

        assertEquals(List.of(
                LocalDate.of(2026, 1, 26),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 9, 8)
        ), resultado.stream().map(feriado -> feriado.date()).toList());
        assertTrue(resultado.stream().allMatch(feriado ->
                feriado.incidencias().get(0).categoria() == FeriadoCategoria.MUNICIPAL));
    }

    @Test
    void consolidaProvedoresEstadualEMunicipalParaLocalidadeConfigurada() {
        LocalizacaoInstalacaoDTO santos = new LocalizacaoInstalacaoDTO("SP", "Santos", "3548500");
        InstallationConfigService config = mock(InstallationConfigService.class);
        when(config.getLocalizacao()).thenReturn(santos);
        BrasilApiClient client = mock(BrasilApiClient.class);
        when(client.buscarFeriadosPorAno(2026)).thenReturn(List.of());
        var service = new FeriadoService(List.of(
                new BrasilApiNacionalFeriadoProvider(client),
                new SaoPauloEstadualFeriadoProvider(),
                new SantosMunicipalFeriadoProvider()
        ), config);

        List<FeriadoDTO> resultado = service.listarFeriados(2026);

        assertTrue(resultado.stream().anyMatch(feriado ->
                feriado.date().equals(LocalDate.of(2026, 7, 9))
                        && feriado.incidencias().get(0).categoria() == FeriadoCategoria.ESTADUAL));
        assertTrue(resultado.stream().anyMatch(feriado ->
                feriado.date().equals(LocalDate.of(2026, 1, 26))
                        && feriado.incidencias().get(0).categoria() == FeriadoCategoria.MUNICIPAL));
        }
}