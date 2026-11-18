package com.planner.habitos.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.habitos.dto.HabitoRequestDTO;
import com.planner.habitos.dto.HabitoResponseDTO;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.repository.HabitoRepository;
import com.planner.habitos.repository.RegistroHabitoRepository;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HabitoService {

    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final UsuarioService usuarioService;

    public HabitoService(HabitoRepository habitoRepository,
                         RegistroHabitoRepository registroHabitoRepository,
                         UsuarioService usuarioService) {
        this.habitoRepository = habitoRepository;
        this.registroHabitoRepository = registroHabitoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public HabitoResponseDTO criar(Long usuarioId, HabitoRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);
        Habito habito = new Habito();
        habito.setUsuario(usuario);
        habito.setNome(dto.nome());
        habito.setDescricao(dto.descricao());
        habito.setFrequencia(dto.frequencia());
        habito.setAtivo(true);
        return toResponseDTO(habitoRepository.save(habito));
    }

    @Transactional(readOnly = true)
    public HabitoResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarHabitoDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<HabitoResponseDTO> listarPorUsuario(Long usuarioId) {
        return habitoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HabitoResponseDTO> listarAtivos(Long usuarioId) {
        return habitoRepository.findByUsuarioIdAndAtivo(usuarioId, true).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public HabitoResponseDTO atualizar(Long usuarioId, Long id, HabitoRequestDTO dto) {
        Habito habito = buscarHabitoDoUsuario(usuarioId, id);
        habito.setNome(dto.nome());
        habito.setDescricao(dto.descricao());
        habito.setFrequencia(dto.frequencia());
        return toResponseDTO(habitoRepository.save(habito));
    }

    @Transactional
    public HabitoResponseDTO ativar(Long usuarioId, Long id) {
        Habito habito = buscarHabitoDoUsuario(usuarioId, id);
        if (habito.isAtivo()) {
            throw new BusinessRuleException("O hábito já está ativo.");
        }
        habito.setAtivo(true);
        return toResponseDTO(habitoRepository.save(habito));
    }

    @Transactional
    public HabitoResponseDTO desativar(Long usuarioId, Long id) {
        Habito habito = buscarHabitoDoUsuario(usuarioId, id);
        if (!habito.isAtivo()) {
            throw new BusinessRuleException("O hábito já está inativo.");
        }
        habito.setAtivo(false);
        return toResponseDTO(habitoRepository.save(habito));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Habito habito = buscarHabitoDoUsuario(usuarioId, id);
        if (!registroHabitoRepository.findByHabitoId(id).isEmpty()) {
            throw new BusinessRuleException("O hábito possui registros vinculados. Remova-os antes de excluir o hábito.");
        }
        habitoRepository.delete(habito);
    }

    @Transactional(readOnly = true)
    public Habito buscarHabitoDoUsuario(Long usuarioId, Long id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito não encontrado."));
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Hábito não encontrado.");
        }
        return habito;
    }

    private HabitoResponseDTO toResponseDTO(Habito habito) {
        return new HabitoResponseDTO(
                habito.getId(),
                habito.getNome(),
                habito.getDescricao(),
                habito.getFrequencia(),
                habito.isAtivo(),
                habito.getCriadoEm()
        );
    }
}
