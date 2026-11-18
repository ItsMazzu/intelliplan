package com.planner.agenda.service;

import com.planner.agenda.dto.CompromissoRequestDTO;
import com.planner.agenda.dto.CompromissoResponseDTO;
import com.planner.agenda.entity.Compromisso;
import com.planner.agenda.repository.CompromissoRepository;
import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final UsuarioService usuarioService;

    public CompromissoService(CompromissoRepository compromissoRepository,
                              UsuarioService usuarioService) {
        this.compromissoRepository = compromissoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public CompromissoResponseDTO criar(Long usuarioId, CompromissoRequestDTO dto) {
        validarHorarios(dto);
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);
        Compromisso compromisso = new Compromisso();
        compromisso.setUsuario(usuario);
        compromisso.setTitulo(dto.titulo());
        compromisso.setDescricao(dto.descricao());
        compromisso.setDataHoraInicio(dto.dataHoraInicio());
        compromisso.setDataHoraFim(dto.dataHoraFim());
        compromisso.setLocal(dto.local());
        return toResponseDTO(compromissoRepository.save(compromisso));
    }

    @Transactional(readOnly = true)
    public CompromissoResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarCompromissoDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<CompromissoResponseDTO> listarPorUsuario(Long usuarioId) {
        return compromissoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompromissoResponseDTO> listarPorPeriodo(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new BusinessRuleException("As datas e horas inicial e final do período são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessRuleException("A data e hora final do período não pode ser anterior à data e hora inicial.");
        }
        return compromissoRepository.findByUsuarioIdAndDataHoraInicioBetween(usuarioId, inicio, fim).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public CompromissoResponseDTO atualizar(Long usuarioId, Long id, CompromissoRequestDTO dto) {
        validarHorarios(dto);
        Compromisso compromisso = buscarCompromissoDoUsuario(usuarioId, id);
        compromisso.setTitulo(dto.titulo());
        compromisso.setDescricao(dto.descricao());
        compromisso.setDataHoraInicio(dto.dataHoraInicio());
        compromisso.setDataHoraFim(dto.dataHoraFim());
        compromisso.setLocal(dto.local());
        return toResponseDTO(compromissoRepository.save(compromisso));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Compromisso compromisso = buscarCompromissoDoUsuario(usuarioId, id);
        compromissoRepository.delete(compromisso);
    }

    private Compromisso buscarCompromissoDoUsuario(Long usuarioId, Long id) {
        Compromisso compromisso = compromissoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compromisso não encontrado."));
        if (!compromisso.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Compromisso não encontrado.");
        }
        return compromisso;
    }

    private void validarHorarios(CompromissoRequestDTO dto) {
        if (dto.dataHoraFim() != null && !dto.dataHoraFim().isAfter(dto.dataHoraInicio())) {
            throw new BusinessRuleException("A data e hora de término deve ser posterior à data e hora de início.");
        }
    }

    private CompromissoResponseDTO toResponseDTO(Compromisso compromisso) {
        return new CompromissoResponseDTO(
                compromisso.getId(),
                compromisso.getTitulo(),
                compromisso.getDescricao(),
                compromisso.getDataHoraInicio(),
                compromisso.getDataHoraFim(),
                compromisso.getLocal(),
                compromisso.getCriadoEm()
        );
    }
}
