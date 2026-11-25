package com.planner.notificacao.service;

import com.planner.agenda.entity.Compromisso;
import com.planner.agenda.repository.CompromissoRepository;
import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.repository.HabitoRepository;
import com.planner.habitos.repository.RegistroHabitoRepository;
import com.planner.notificacao.entity.Notificacao;
import com.planner.notificacao.repository.NotificacaoRepository;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.repository.ProjetoRepository;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServiceTest {

    @Test
    void deveEscoparBuscaDeNotificacaoAoUsuarioAutenticado() {
        UsuarioContextService contexto = mock(UsuarioContextService.class);
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        NotificacaoService service = criarService(contexto, notificacaoRepository);
        when(contexto.obterUsuarioIdAtual()).thenReturn(15L);
        when(notificacaoRepository.findByIdAndUsuarioId(91L, 15L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.marcarComoLida(91L));

        verify(notificacaoRepository).findByIdAndUsuarioId(91L, 15L);
        verify(notificacaoRepository, never()).findById(91L);
    }

    @Test
    void devePersistirDataDeLeituraDaNotificacaoDoUsuario() {
        UsuarioContextService contexto = mock(UsuarioContextService.class);
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        NotificacaoService service = criarService(contexto, notificacaoRepository);
        when(contexto.obterUsuarioIdAtual()).thenReturn(15L);
        Notificacao notificacao = new Notificacao();
        notificacao.setId(91L);
        notificacao.setTitulo("Prazo próximo");
        notificacao.setDescricao("A tarefa vence hoje.");
        notificacao.setTipo(Notificacao.Tipo.LEMBRETE_TAREFA);
        notificacao.setUrlDestino("/tarefas");
        when(notificacaoRepository.findByIdAndUsuarioId(91L, 15L)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(notificacao)).thenReturn(notificacao);

        service.marcarComoLida(91L);

        verify(notificacaoRepository).save(notificacao);
        org.junit.jupiter.api.Assertions.assertNotNull(notificacao.getLidaEm());
    }

    @Test
    void deveReconciliarEventosUsandoChaveIdempotente() {
        UsuarioContextService contexto = mock(UsuarioContextService.class);
        TarefaRepository tarefaRepository = mock(TarefaRepository.class);
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        NotificacaoService service = criarService(
                contexto,
                tarefaRepository,
                notificacaoRepository,
                mock(ProjetoRepository.class),
                mock(CompromissoRepository.class),
                mock(HabitoRepository.class),
                mock(RegistroHabitoRepository.class)
        );
        when(contexto.obterUsuarioIdAtual()).thenReturn(15L);
        Tarefa tarefa = new Tarefa();
        tarefa.setId(41L);
        tarefa.setTitulo("Entregar relatório");
        tarefa.setDataVencimento(LocalDate.now());
        when(tarefaRepository.findByUsuarioIdAndDataVencimentoLessThanEqualAndStatusIn(
                eq(15L), any(LocalDate.class), anyCollection())).thenReturn(java.util.List.of(tarefa));
        when(notificacaoRepository.findTop30ByUsuarioIdOrderByCriadaEmDesc(15L)).thenReturn(java.util.List.of());
        when(notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(15L)).thenReturn(0L);

        service.sincronizarEListarDoUsuarioAtual();
        service.sincronizarEListarDoUsuarioAtual();

        verify(notificacaoRepository, times(2)).inserirSeNovoEvento(
                eq("Lembrete de tarefa"),
                anyString(),
                eq(Notificacao.Tipo.LEMBRETE_TAREFA.name()),
                eq("LEMBRETE_TAREFA:TAREFA:41:" + LocalDate.now()),
                eq("TAREFA"),
                eq(41L),
                eq("/tarefas"),
                eq(15L)
        );
        verify(notificacaoRepository, times(2)).findTop30ByUsuarioIdOrderByCriadaEmDesc(15L);
    }

    @Test
    void deveGerarAlertasReaisDeTarefaCompromissoProjetoEHabito() {
        UsuarioContextService contexto = mock(UsuarioContextService.class);
        TarefaRepository tarefaRepository = mock(TarefaRepository.class);
        ProjetoRepository projetoRepository = mock(ProjetoRepository.class);
        CompromissoRepository compromissoRepository = mock(CompromissoRepository.class);
        HabitoRepository habitoRepository = mock(HabitoRepository.class);
        RegistroHabitoRepository registroHabitoRepository = mock(RegistroHabitoRepository.class);
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        NotificacaoService service = criarService(
                contexto,
                tarefaRepository,
                notificacaoRepository,
                projetoRepository,
                compromissoRepository,
                habitoRepository,
                registroHabitoRepository
        );
        when(contexto.obterUsuarioIdAtual()).thenReturn(15L);

        Tarefa tarefaAtrasada = new Tarefa();
        tarefaAtrasada.setId(41L);
        tarefaAtrasada.setTitulo("Entregar relatório");
        tarefaAtrasada.setDataVencimento(LocalDate.now().minusDays(1));
        when(tarefaRepository.findByUsuarioIdAndDataVencimentoLessThanEqualAndStatusIn(
                eq(15L), any(LocalDate.class), anyCollection())).thenReturn(java.util.List.of(tarefaAtrasada));

        Compromisso compromisso = new Compromisso();
        compromisso.setId(51L);
        compromisso.setTitulo("Consulta");
        compromisso.setDataHoraInicio(LocalDateTime.now().plusHours(4));
        when(compromissoRepository.findUpcomingByUsuarioId(
                eq(15L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(compromisso));

        Projeto projeto = new Projeto();
        projeto.setId(61L);
        projeto.setNome("Projeto final");
        projeto.setDataConclusao(LocalDate.now().plusDays(4));
        when(projetoRepository.findByUsuarioIdAndStatusInAndDataConclusaoBetween(
                eq(15L), anyCollection(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.List.of(projeto));

        Habito habito = new Habito();
        habito.setId(71L);
        habito.setNome("Leitura");
        habito.setFrequencia(Habito.Frequencia.DIARIO);
        when(habitoRepository.findByUsuarioIdAndAtivo(15L, true)).thenReturn(java.util.List.of(habito));
        when(registroHabitoRepository.findDashboardRegistros(
                eq(java.util.List.of(71L)), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.List.of());
        when(notificacaoRepository.findTop30ByUsuarioIdOrderByCriadaEmDesc(15L)).thenReturn(java.util.List.of());
        when(notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(15L)).thenReturn(0L);

        service.sincronizarEListarDoUsuarioAtual();

        ArgumentCaptor<String> tipoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoRepository, times(4)).inserirSeNovoEvento(
                anyString(),
                anyString(),
                tipoCaptor.capture(),
                anyString(),
                anyString(),
                anyLong(),
                urlCaptor.capture(),
                eq(15L)
        );
        assertEquals(Set.of(
                Notificacao.Tipo.TAREFA_ATRASADA.name(),
                Notificacao.Tipo.LEMBRETE_COMPROMISSO.name(),
                Notificacao.Tipo.ALERTA_PROJETO.name(),
                Notificacao.Tipo.LEMBRETE_HABITO.name()
        ), new HashSet<>(tipoCaptor.getAllValues()));
        assertEquals(Set.of(
                "/tarefas",
                "/agenda?ano=" + compromisso.getDataHoraInicio().getYear()
                        + "&mes=" + compromisso.getDataHoraInicio().getMonthValue(),
                "/projetos",
                "/habitos"
        ), new HashSet<>(urlCaptor.getAllValues()));
    }

    private NotificacaoService criarService(UsuarioContextService contexto,
                                            NotificacaoRepository notificacaoRepository) {
        return criarService(
                contexto,
                mock(TarefaRepository.class),
                notificacaoRepository,
                mock(ProjetoRepository.class),
                mock(CompromissoRepository.class),
                mock(HabitoRepository.class),
                mock(RegistroHabitoRepository.class)
        );
    }

    private NotificacaoService criarService(UsuarioContextService contexto,
                                            TarefaRepository tarefaRepository,
                                            NotificacaoRepository notificacaoRepository,
                                            ProjetoRepository projetoRepository,
                                            CompromissoRepository compromissoRepository,
                                            HabitoRepository habitoRepository,
                                            RegistroHabitoRepository registroHabitoRepository) {
        return new NotificacaoService(
                contexto,
                tarefaRepository,
                projetoRepository,
                compromissoRepository,
                habitoRepository,
                registroHabitoRepository,
                notificacaoRepository
        );
    }
}
