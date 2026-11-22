package com.planner.integracao.feriados.service;

import com.planner.integracao.feriados.client.BrasilApiClient;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeriadoServiceTest {

    @Test
    void deveReutilizarRespostaBemSucedidaPorAno() {
        BrasilApiClient client = mock(BrasilApiClient.class);
        FeriadoService service = new FeriadoService(client);
        List<FeriadoDTO> feriados = List.of(
                new FeriadoDTO(LocalDate.of(2026, 1, 1), "Confraternização Universal", "national")
        );
        when(client.buscarFeriadosPorAno(2026)).thenReturn(feriados);

        assertEquals(feriados, service.listarFeriados(2026));
        assertEquals(feriados, service.listarFeriados(2026));

        verify(client, times(1)).buscarFeriadosPorAno(2026);
    }

    @Test
    void naoDeveArmazenarRespostaVazia() {
        BrasilApiClient client = mock(BrasilApiClient.class);
        FeriadoService service = new FeriadoService(client);
        List<FeriadoDTO> feriados = List.of(
                new FeriadoDTO(LocalDate.of(2026, 1, 1), "Confraternização Universal", "national")
        );
        when(client.buscarFeriadosPorAno(2026)).thenReturn(List.<FeriadoDTO>of()).thenReturn(feriados);

        assertEquals(List.of(), service.listarFeriados(2026));
        assertEquals(feriados, service.listarFeriados(2026));

        verify(client, times(2)).buscarFeriadosPorAno(2026);
    }

    @Test
    void naoDeveArmazenarFalhaDaApi() {
        BrasilApiClient client = mock(BrasilApiClient.class);
        FeriadoService service = new FeriadoService(client);
        List<FeriadoDTO> feriados = List.of(
                new FeriadoDTO(LocalDate.of(2026, 1, 1), "Confraternização Universal", "national")
        );
        when(client.buscarFeriadosPorAno(2026))
                .thenThrow(new ResourceAccessException("API indisponível"))
                .thenReturn(feriados);

        assertEquals(List.of(), service.listarFeriados(2026));
        assertEquals(feriados, service.listarFeriados(2026));

        verify(client, times(2)).buscarFeriadosPorAno(2026);
    }
}
