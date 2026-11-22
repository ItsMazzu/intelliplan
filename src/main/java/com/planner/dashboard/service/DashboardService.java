package com.planner.dashboard.service;

import com.planner.agenda.entity.Compromisso;
import com.planner.agenda.repository.CompromissoRepository;
import com.planner.common.context.UsuarioContextService;
import com.planner.dashboard.dto.DashboardDadosDTO;
import com.planner.dashboard.dto.DiaCalendarioDTO;
import com.planner.dashboard.dto.DiaHabitoDTO;
import com.planner.dashboard.dto.MateriaItemDTO;
import com.planner.dashboard.dto.ProjetoItemDTO;
import com.planner.dashboard.dto.TarefaItemDTO;
import com.planner.estudos.entity.ConteudoEstudo;
import com.planner.estudos.entity.Materia;
import com.planner.estudos.repository.ConteudoEstudoRepository;
import com.planner.estudos.repository.MateriaRepository;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.repository.HabitoRepository;
import com.planner.habitos.repository.RegistroHabitoRepository;
import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.service.FeriadoService;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.repository.ProjetoRepository;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.repository.TarefaRepository;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UsuarioContextService usuarioContextService;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final MateriaRepository materiaRepository;
    private final ConteudoEstudoRepository conteudoEstudoRepository;
    private final CompromissoRepository compromissoRepository;
    private final FeriadoService feriadoService;

    public DashboardService(UsuarioContextService usuarioContextService,
                            UsuarioRepository usuarioRepository,
                            TarefaRepository tarefaRepository,
                            ProjetoRepository projetoRepository,
                            HabitoRepository habitoRepository,
                            RegistroHabitoRepository registroHabitoRepository,
                            MateriaRepository materiaRepository,
                            ConteudoEstudoRepository conteudoEstudoRepository,
                            CompromissoRepository compromissoRepository,
                            FeriadoService feriadoService) {
        this.usuarioContextService = usuarioContextService;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.habitoRepository = habitoRepository;
        this.registroHabitoRepository = registroHabitoRepository;
        this.materiaRepository = materiaRepository;
        this.conteudoEstudoRepository = conteudoEstudoRepository;
        this.compromissoRepository = compromissoRepository;
        this.feriadoService = feriadoService;
    }

    @Transactional(readOnly = true)
    public DashboardDadosDTO obterDadosDashboard() {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            return criarDadosPadrao();
        }

        Long usuarioId = usuarioIdOpt.get();
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return criarDadosPadrao();
        }

        Usuario usuario = usuarioOpt.get();
        String usuarioNome = usuario.getNome() != null && !usuario.getNome().isBlank() ? usuario.getNome() : "Usuário";
        String primeiroNome = usuarioNome.trim().split("\\s+")[0];

        List<Tarefa> tarefas = tarefaRepository.findByUsuarioId(usuarioId);
        List<Projeto> projetos = projetoRepository.findByUsuarioId(usuarioId);
        List<Habito> habitos = habitoRepository.findByUsuarioId(usuarioId);
        List<Materia> materiasEntities = materiaRepository.findByUsuarioId(usuarioId);

        LocalDate hoje = LocalDate.now();

        long totalTarefas = tarefas.size();
        long totalProjetos = projetos.size();
        long totalHabitos = habitos.size();

        List<DiaHabitoDTO> diasHabitos = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate data = hoje.minusDays(i);
            String inicial = obterInicialDiaSemana(data);
            boolean concluido = habitos.stream()
                    .filter(Habito::isAtivo)
                    .anyMatch(h -> registroHabitoRepository.existsByHabitoIdAndDataRegistro(h.getId(), data));
            diasHabitos.add(new DiaHabitoDTO(concluido, inicial));
        }

        String mesAtual = formatarMesAno(hoje);

        LocalDateTime inicioMes = YearMonth.from(hoje).atDay(1).atStartOfDay();
        LocalDateTime fimMes = YearMonth.from(hoje).atEndOfMonth().atTime(23, 59, 59);
        List<Compromisso> compromissosMes = compromissoRepository.findByUsuarioIdAndDataHoraInicioBetween(usuarioId, inicioMes, fimMes);
        Set<LocalDate> datasComCompromisso = compromissosMes.stream()
                .map(c -> c.getDataHoraInicio().toLocalDate())
                .collect(Collectors.toSet());

        List<FeriadoDTO> feriadosAno = feriadoService.listarFeriados(hoje.getYear());
        Set<LocalDate> datasFeriados = feriadosAno.stream()
                .map(FeriadoDTO::date)
                .filter(d -> d != null && d.getMonth() == hoje.getMonth() && d.getYear() == hoje.getYear())
                .collect(Collectors.toSet());

        int totalDiasNoMes = YearMonth.from(hoje).lengthOfMonth();
        List<DiaCalendarioDTO> diasCalendario = new ArrayList<>();
        for (int dia = 1; dia <= totalDiasNoMes; dia++) {
            LocalDate dataDia = LocalDate.of(hoje.getYear(), hoje.getMonth(), dia);
            boolean isHoje = dataDia.equals(hoje);
            boolean temEvento = datasComCompromisso.contains(dataDia) || datasFeriados.contains(dataDia);
            diasCalendario.add(new DiaCalendarioDTO(dia, isHoje, temEvento));
        }

        List<TarefaItemDTO> tarefasHoje = tarefas.stream()
                .filter(t -> (t.getDataVencimento() != null && t.getDataVencimento().equals(hoje))
                        || (t.getStatus() != Tarefa.Status.CONCLUIDA && t.getStatus() != Tarefa.Status.CANCELADA))
                .limit(5)
                .map(t -> new TarefaItemDTO(
                        t.getId(),
                        t.getTitulo(),
                        t.getStatus() == Tarefa.Status.CONCLUIDA,
                        t.getProjeto() != null ? t.getProjeto().getNome() : "Geral"
                ))
                .toList();

        long tarefasConcluidas = tarefas.stream().filter(t -> t.getStatus() == Tarefa.Status.CONCLUIDA).count();
        int percTarefas = totalTarefas > 0 ? (int) ((tarefasConcluidas * 100) / totalTarefas) : 0;
        String progressoTarefas = percTarefas + "%";

        int totalConteudos = 0;
        int conteudosConcluidos = 0;
        for (Materia m : materiasEntities) {
            List<ConteudoEstudo> conteudos = conteudoEstudoRepository.findByMateriaId(m.getId());
            totalConteudos += conteudos.size();
            conteudosConcluidos += (int) conteudos.stream().filter(c -> c.getStatus() == ConteudoEstudo.Status.CONCLUIDO).count();
        }
        int percEstudos = totalConteudos > 0 ? (conteudosConcluidos * 100) / totalConteudos : 0;
        String progressoEstudos = percEstudos + "%";

        List<Habito> habitosAtivos = habitos.stream().filter(Habito::isAtivo).toList();
        long habitosHojeConcluidos = habitosAtivos.stream()
                .filter(h -> registroHabitoRepository.existsByHabitoIdAndDataRegistro(h.getId(), hoje))
                .count();
        int percHabitos = !habitosAtivos.isEmpty() ? (int) ((habitosHojeConcluidos * 100) / habitosAtivos.size()) : 0;
        String progressoHabitos = percHabitos + "%";

        long projetosConcluidos = projetos.stream().filter(p -> p.getStatus() == Projeto.Status.CONCLUIDO).count();
        int percProjetos = totalProjetos > 0 ? (int) ((projetosConcluidos * 100) / totalProjetos) : 0;
        String progressoProjetos = percProjetos + "%";

        int progressoGeral = (percTarefas + percEstudos + percHabitos + percProjetos) / 4;

        List<ProjetoItemDTO> projetosAtivos = projetos.stream()
                .filter(p -> p.getStatus() == Projeto.Status.EM_ANDAMENTO || p.getStatus() == Projeto.Status.PLANEJADO)
                .limit(5)
                .map(p -> {
                    List<Tarefa> tarefasProjeto = tarefaRepository.findByProjetoId(p.getId());
                    long concluidas = tarefasProjeto.stream().filter(t -> t.getStatus() == Tarefa.Status.CONCLUIDA).count();
                    int progresso = !tarefasProjeto.isEmpty() ? (int) ((concluidas * 100) / tarefasProjeto.size()) : 0;
                    return new ProjetoItemDTO(
                            p.getNome(),
                            p.getDescricao() != null ? p.getDescricao() : "",
                            progresso
                    );
                })
                .toList();

        List<MateriaItemDTO> materias = materiasEntities.stream()
                .limit(5)
                .map(m -> {
                    List<ConteudoEstudo> conteudos = conteudoEstudoRepository.findByMateriaId(m.getId());
                    long concluidos = conteudos.stream().filter(c -> c.getStatus() == ConteudoEstudo.Status.CONCLUIDO).count();
                    int progresso = !conteudos.isEmpty() ? (int) ((concluidos * 100) / conteudos.size()) : 0;
                    String conteudoAtual = conteudos.stream()
                            .filter(c -> c.getStatus() == ConteudoEstudo.Status.EM_ANDAMENTO)
                            .map(ConteudoEstudo::getTitulo)
                            .findFirst()
                            .orElseGet(() -> conteudos.stream()
                                    .filter(c -> c.getStatus() == ConteudoEstudo.Status.NAO_INICIADO)
                                    .map(ConteudoEstudo::getTitulo)
                                    .findFirst()
                                    .orElse("Sem conteúdos cadastrados"));
                    return new MateriaItemDTO(m.getNome(), conteudoAtual, progresso);
                })
                .toList();

        return new DashboardDadosDTO(
                usuarioNome,
                primeiroNome,
                totalTarefas,
                totalProjetos,
                totalHabitos,
                diasHabitos,
                mesAtual,
                diasCalendario,
                tarefasHoje,
                progressoGeral,
                progressoTarefas,
                progressoEstudos,
                progressoHabitos,
                progressoProjetos,
                projetosAtivos,
                materias
        );
    }

    private DashboardDadosDTO criarDadosPadrao() {
        LocalDate hoje = LocalDate.now();
        List<DiaHabitoDTO> diasHabitos = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate data = hoje.minusDays(i);
            diasHabitos.add(new DiaHabitoDTO(false, obterInicialDiaSemana(data)));
        }

        String mesAtual = formatarMesAno(hoje);

        List<FeriadoDTO> feriadosAno = feriadoService.listarFeriados(hoje.getYear());
        Set<LocalDate> datasFeriados = feriadosAno.stream()
                .map(FeriadoDTO::date)
                .filter(d -> d != null && d.getMonth() == hoje.getMonth() && d.getYear() == hoje.getYear())
                .collect(Collectors.toSet());

        int totalDiasNoMes = YearMonth.from(hoje).lengthOfMonth();
        List<DiaCalendarioDTO> diasCalendario = new ArrayList<>();
        for (int dia = 1; dia <= totalDiasNoMes; dia++) {
            LocalDate dataDia = LocalDate.of(hoje.getYear(), hoje.getMonth(), dia);
            boolean isHoje = dataDia.equals(hoje);
            boolean temEvento = datasFeriados.contains(dataDia);
            diasCalendario.add(new DiaCalendarioDTO(dia, isHoje, temEvento));
        }

        return new DashboardDadosDTO(
                "Usuário",
                "Usuário",
                0L,
                0L,
                0L,
                diasHabitos,
                mesAtual,
                diasCalendario,
                Collections.emptyList(),
                0,
                "0%",
                "0%",
                "0%",
                "0%",
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private String obterInicialDiaSemana(LocalDate data) {
        return switch (data.getDayOfWeek()) {
            case SUNDAY -> "D";
            case MONDAY -> "S";
            case TUESDAY -> "T";
            case WEDNESDAY -> "Q";
            case THURSDAY -> "Q";
            case FRIDAY -> "S";
            case SATURDAY -> "S";
        };
    }

    private String formatarMesAno(LocalDate data) {
        String nomeMes = data.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        nomeMes = Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1);
        return nomeMes + " de " + data.getYear();
    }
}
