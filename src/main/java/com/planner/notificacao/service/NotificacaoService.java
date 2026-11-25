package com.planner.notificacao.service;

import com.planner.agenda.entity.Compromisso;
import com.planner.agenda.repository.CompromissoRepository;
import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.repository.HabitoRepository;
import com.planner.habitos.repository.RegistroHabitoDashboardProjection;
import com.planner.habitos.repository.RegistroHabitoRepository;
import com.planner.notificacao.dto.NotificacaoListaResponseDTO;
import com.planner.notificacao.dto.NotificacaoResponseDTO;
import com.planner.notificacao.entity.Notificacao;
import com.planner.notificacao.repository.NotificacaoRepository;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.repository.ProjetoRepository;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final Set<Tarefa.Status> STATUS_ABERTOS = Set.of(
            Tarefa.Status.PENDENTE,
            Tarefa.Status.EM_ANDAMENTO
    );
    private static final Set<Projeto.Status> STATUS_PROJETOS_ABERTOS = Set.of(
            Projeto.Status.PLANEJADO,
            Projeto.Status.EM_ANDAMENTO
    );

    private final UsuarioContextService usuarioContextService;
    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final CompromissoRepository compromissoRepository;
    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(UsuarioContextService usuarioContextService,
                              TarefaRepository tarefaRepository,
                              ProjetoRepository projetoRepository,
                              CompromissoRepository compromissoRepository,
                              HabitoRepository habitoRepository,
                              RegistroHabitoRepository registroHabitoRepository,
                              NotificacaoRepository notificacaoRepository) {
        this.usuarioContextService = usuarioContextService;
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.compromissoRepository = compromissoRepository;
        this.habitoRepository = habitoRepository;
        this.registroHabitoRepository = registroHabitoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public NotificacaoListaResponseDTO listarDoUsuarioAtual() {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<NotificacaoResponseDTO> notificacoes = notificacaoRepository
                .findTop30ByUsuarioIdOrderByCriadaEmDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
        long naoLidas = notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(usuarioId);
        return new NotificacaoListaResponseDTO(notificacoes, naoLidas);
    }

    @Transactional
    public NotificacaoListaResponseDTO sincronizarEListarDoUsuarioAtual() {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        criarLembretesTarefas(usuarioId, hoje);
        criarLembretesCompromissos(usuarioId, agora);
        criarAlertasProjetos(usuarioId, hoje);
        criarLembretesHabitos(usuarioId, hoje);

        List<NotificacaoResponseDTO> notificacoes = notificacaoRepository
                .findTop30ByUsuarioIdOrderByCriadaEmDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
        long naoLidas = notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(usuarioId);
        return new NotificacaoListaResponseDTO(notificacoes, naoLidas);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada."));
        if (notificacao.getLidaEm() == null) {
            notificacao.setLidaEm(LocalDateTime.now());
            notificacaoRepository.save(notificacao);
        }
        return toResponse(notificacao);
    }

    @Transactional
    public long marcarTodasComoLidas() {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }

    private void criarLembretesTarefas(Long usuarioId, LocalDate hoje) {
        List<Tarefa> tarefas = tarefaRepository.findByUsuarioIdAndDataVencimentoLessThanEqualAndStatusIn(
                usuarioId,
                hoje.plusDays(3),
                STATUS_ABERTOS
        );
        for (Tarefa tarefa : tarefas) {
            LocalDate vencimento = tarefa.getDataVencimento();
            if (vencimento == null) continue;
            boolean atrasada = vencimento.isBefore(hoje);
            Notificacao.Tipo tipo = atrasada
                    ? Notificacao.Tipo.TAREFA_ATRASADA
                    : Notificacao.Tipo.LEMBRETE_TAREFA;
            String titulo = atrasada ? "Tarefa atrasada" : "Lembrete de tarefa";
            String descricao = "“" + tarefa.getTitulo() + "” "
                    + (atrasada ? "venceu em " : "vence em ")
                    + vencimento.format(DATA) + ".";
            criarSeNovo(
                    usuarioId,
                    tipo,
                    "TAREFA",
                    tarefa.getId(),
                    vencimento.toString(),
                    titulo,
                    descricao,
                    "/tarefas"
            );
        }
    }

    private void criarLembretesCompromissos(Long usuarioId, LocalDateTime agora) {
        List<Compromisso> compromissos = compromissoRepository.findUpcomingByUsuarioId(
                usuarioId,
                agora,
                agora.plusHours(24)
        );
        for (Compromisso compromisso : compromissos) {
            LocalDateTime inicio = compromisso.getDataHoraInicio();
            String destino = "/agenda?ano=" + inicio.getYear() + "&mes=" + inicio.getMonthValue();
            criarSeNovo(
                    usuarioId,
                    Notificacao.Tipo.LEMBRETE_COMPROMISSO,
                    "COMPROMISSO",
                    compromisso.getId(),
                    inicio.toString(),
                    "Compromisso próximo",
                    "“" + compromisso.getTitulo() + "” começa em " + inicio.format(DATA_HORA) + ".",
                    destino
            );
        }
    }

    private void criarAlertasProjetos(Long usuarioId, LocalDate hoje) {
        List<Projeto> projetos = projetoRepository.findByUsuarioIdAndStatusInAndDataConclusaoBetween(
                usuarioId,
                STATUS_PROJETOS_ABERTOS,
                hoje,
                hoje.plusDays(7)
        );
        for (Projeto projeto : projetos) {
            LocalDate prazo = projeto.getDataConclusao();
            if (prazo == null) continue;
            criarSeNovo(
                    usuarioId,
                    Notificacao.Tipo.ALERTA_PROJETO,
                    "PROJETO",
                    projeto.getId(),
                    prazo.toString(),
                    "Prazo de projeto próximo",
                    "“" + projeto.getNome() + "” tem prazo até " + prazo.format(DATA) + ".",
                    "/projetos"
            );
        }
    }

    private void criarLembretesHabitos(Long usuarioId, LocalDate hoje) {
        List<Habito> habitos = habitoRepository.findByUsuarioIdAndAtivo(usuarioId, true);
        if (habitos.isEmpty()) return;

        List<Long> habitoIds = habitos.stream().map(Habito::getId).toList();
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate inicioMes = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate inicioHistorico = inicioSemana.isBefore(inicioMes) ? inicioSemana : inicioMes;
        List<RegistroHabitoDashboardProjection> registros = registroHabitoRepository.findDashboardRegistros(
                habitoIds,
                inicioHistorico,
                hoje
        );
        Map<Long, Habito.Frequencia> frequencias = habitos.stream()
                .collect(Collectors.toMap(Habito::getId, Habito::getFrequencia));
        Set<String> habitosRegistradosNoCiclo = registros.stream()
                .map(registro -> {
                    Habito.Frequencia frequencia = frequencias.get(registro.habitoId());
                    return frequencia == null ? null : registro.habitoId() + ":"
                            + inicioDoCiclo(frequencia, registro.dataRegistro());
                })
                .filter(chave -> chave != null)
                .collect(Collectors.toSet());

        for (Habito habito : habitos) {
            LocalDate inicioCiclo = inicioDoCiclo(habito.getFrequencia(), hoje);
            if (habitosRegistradosNoCiclo.contains(habito.getId() + ":" + inicioCiclo)) continue;
            String frequencia = switch (habito.getFrequencia()) {
                case DIARIO -> "hoje";
                case SEMANAL -> "nesta semana";
                case MENSAL -> "neste mês";
            };
            criarSeNovo(
                    usuarioId,
                    Notificacao.Tipo.LEMBRETE_HABITO,
                    "HABITO",
                    habito.getId(),
                    inicioCiclo.toString(),
                    "Lembrete de hábito",
                    "Ainda não há registro de “" + habito.getNome() + "” " + frequencia + ".",
                    "/habitos"
            );
        }
    }

    private LocalDate inicioDoCiclo(Habito.Frequencia frequencia, LocalDate data) {
        return switch (frequencia) {
            case DIARIO -> data;
            case SEMANAL -> data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MENSAL -> YearMonth.from(data).atDay(1);
        };
    }

    private void criarSeNovo(Long usuarioId,
                             Notificacao.Tipo tipo,
                             String tipoReferencia,
                             Long referenciaId,
                             String ocorrencia,
                             String titulo,
                             String descricao,
                             String urlDestino) {
        String chaveEvento = tipo.name() + ":" + tipoReferencia + ":" + referenciaId + ":" + ocorrencia;
        notificacaoRepository.inserirSeNovoEvento(
                titulo,
                descricao,
                tipo.name(),
                chaveEvento,
                tipoReferencia,
                referenciaId,
                urlDestino,
                usuarioId
        );
    }

    private NotificacaoResponseDTO toResponse(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getDescricao(),
                notificacao.getTipo(),
                notificacao.getCriadaEm(),
                notificacao.getLidaEm(),
                notificacao.getUrlDestino()
        );
    }
}
