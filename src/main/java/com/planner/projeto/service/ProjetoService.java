package com.planner.projeto.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.projeto.dto.ProjetoRequestDTO;
import com.planner.projeto.dto.ProjetoResponseDTO;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.repository.ProjetoRepository;
import com.planner.tarefa.repository.TarefaRepository;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;

    public ProjetoService(ProjetoRepository projetoRepository,
                          TarefaRepository tarefaRepository,
                          UsuarioService usuarioService) {
        this.projetoRepository = projetoRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public ProjetoResponseDTO criar(Long usuarioId, ProjetoRequestDTO dto) {
        validarDatas(dto);
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);
        Projeto projeto = new Projeto();
        projeto.setUsuario(usuario);
        projeto.setNome(dto.nome());
        projeto.setDescricao(dto.descricao());
        projeto.setStatus(dto.status());
        projeto.setDataInicio(dto.dataInicio());
        projeto.setDataConclusao(dto.dataConclusao());
        return toResponseDTO(projetoRepository.save(projeto));
    }

    @Transactional(readOnly = true)
    public ProjetoResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarProjetoDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listarPorUsuario(Long usuarioId) {
        return projetoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listarPorStatus(Long usuarioId, Projeto.Status status) {
        return projetoRepository.findByUsuarioIdAndStatus(usuarioId, status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public ProjetoResponseDTO atualizar(Long usuarioId, Long id, ProjetoRequestDTO dto) {
        validarDatas(dto);
        Projeto projeto = buscarProjetoDoUsuario(usuarioId, id);
        projeto.setNome(dto.nome());
        projeto.setDescricao(dto.descricao());
        projeto.setStatus(dto.status());
        projeto.setDataInicio(dto.dataInicio());
        projeto.setDataConclusao(dto.dataConclusao());
        return toResponseDTO(projetoRepository.save(projeto));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Projeto projeto = buscarProjetoDoUsuario(usuarioId, id);
        if (!tarefaRepository.findByProjetoId(id).isEmpty()) {
            throw new BusinessRuleException("O projeto possui tarefas vinculadas. Remova-as antes de excluir o projeto.");
        }
        projetoRepository.delete(projeto);
    }

    @Transactional(readOnly = true)
    public Projeto buscarProjetoDoUsuario(Long usuarioId, Long id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
        if (!projeto.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Projeto não encontrado.");
        }
        return projeto;
    }

    private void validarDatas(ProjetoRequestDTO dto) {
        if (dto.dataInicio() != null && dto.dataConclusao() != null
                && !dto.dataConclusao().isAfter(dto.dataInicio())) {
            throw new BusinessRuleException("A data de conclusão deve ser posterior à data de início.");
        }
    }

    private ProjetoResponseDTO toResponseDTO(Projeto projeto) {
        return new ProjetoResponseDTO(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getStatus(),
                projeto.getDataInicio(),
                projeto.getDataConclusao(),
                projeto.getCriadoEm()
        );
    }
}
