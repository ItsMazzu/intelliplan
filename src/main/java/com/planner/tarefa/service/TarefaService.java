package com.planner.tarefa.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.service.ProjetoService;
import com.planner.tarefa.dto.TarefaRequestDTO;
import com.planner.tarefa.dto.TarefaResponseDTO;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.repository.TarefaRepository;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public TarefaService(TarefaRepository tarefaRepository,
                         ProjetoService projetoService,
                         UsuarioService usuarioService) {
        this.tarefaRepository = tarefaRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public TarefaResponseDTO criar(Long usuarioId, TarefaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);
        Projeto projeto = null;
        if (dto.projetoId() != null) {
            projeto = projetoService.buscarProjetoDoUsuario(usuarioId, dto.projetoId());
        }

        Tarefa tarefa = new Tarefa();
        tarefa.setUsuario(usuario);
        tarefa.setProjeto(projeto);
        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setStatus(dto.status());
        tarefa.setPrioridade(dto.prioridade());
        tarefa.setDataVencimento(dto.dataVencimento());

        return toResponseDTO(tarefaRepository.save(tarefa));
    }

    @Transactional(readOnly = true)
    public TarefaResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarTarefaDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorUsuario(Long usuarioId) {
        return tarefaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorStatus(Long usuarioId, Tarefa.Status status) {
        return tarefaRepository.findByUsuarioIdAndStatus(usuarioId, status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorProjeto(Long usuarioId, Long projetoId) {
        projetoService.buscarProjetoDoUsuario(usuarioId, projetoId);
        return tarefaRepository.findByProjetoId(projetoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessRuleException("As datas inicial e final do período são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessRuleException("A data final do período não pode ser anterior à data inicial.");
        }
        return tarefaRepository.findByUsuarioIdAndDataVencimentoBetween(usuarioId, inicio, fim).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public TarefaResponseDTO atualizar(Long usuarioId, Long id, TarefaRequestDTO dto) {
        Tarefa tarefa = buscarTarefaDoUsuario(usuarioId, id);

        if (dto.projetoId() != null) {
            if (tarefa.getProjeto() == null || !tarefa.getProjeto().getId().equals(dto.projetoId())) {
                Projeto projeto = projetoService.buscarProjetoDoUsuario(usuarioId, dto.projetoId());
                tarefa.setProjeto(projeto);
            }
        } else {
            tarefa.setProjeto(null);
        }

        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setStatus(dto.status());
        tarefa.setPrioridade(dto.prioridade());
        tarefa.setDataVencimento(dto.dataVencimento());

        return toResponseDTO(tarefaRepository.save(tarefa));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Tarefa tarefa = buscarTarefaDoUsuario(usuarioId, id);
        tarefaRepository.delete(tarefa);
    }

    private Tarefa buscarTarefaDoUsuario(Long usuarioId, Long id) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));
        if (!tarefa.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Tarefa não encontrada.");
        }
        return tarefa;
    }

    private TarefaResponseDTO toResponseDTO(Tarefa tarefa) {
        Long projetoId = tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null;
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getDataVencimento(),
                tarefa.getCriadoEm(),
                projetoId
        );
    }
}
