package com.planner.estudos.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.estudos.dto.MateriaRequestDTO;
import com.planner.estudos.dto.MateriaResponseDTO;
import com.planner.estudos.entity.Materia;
import com.planner.estudos.repository.ConteudoEstudoRepository;
import com.planner.estudos.repository.MateriaRepository;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final ConteudoEstudoRepository conteudoEstudoRepository;
    private final UsuarioService usuarioService;

    public MateriaService(MateriaRepository materiaRepository,
                          ConteudoEstudoRepository conteudoEstudoRepository,
                          UsuarioService usuarioService) {
        this.materiaRepository = materiaRepository;
        this.conteudoEstudoRepository = conteudoEstudoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public MateriaResponseDTO criar(Long usuarioId, MateriaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);
        Materia materia = new Materia();
        materia.setUsuario(usuario);
        materia.setNome(dto.nome());
        materia.setDescricao(dto.descricao());
        return toResponseDTO(materiaRepository.save(materia));
    }

    @Transactional(readOnly = true)
    public MateriaResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarMateriaDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<MateriaResponseDTO> listarPorUsuario(Long usuarioId) {
        return materiaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public MateriaResponseDTO atualizar(Long usuarioId, Long id, MateriaRequestDTO dto) {
        Materia materia = buscarMateriaDoUsuario(usuarioId, id);
        materia.setNome(dto.nome());
        materia.setDescricao(dto.descricao());
        return toResponseDTO(materiaRepository.save(materia));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Materia materia = buscarMateriaDoUsuario(usuarioId, id);
        if (!conteudoEstudoRepository.findByMateriaId(id).isEmpty()) {
            throw new BusinessRuleException("A matéria possui conteúdos de estudo vinculados. Remova-os antes de excluir a matéria.");
        }
        materiaRepository.delete(materia);
    }

    @Transactional(readOnly = true)
    public Materia buscarMateriaDoUsuario(Long usuarioId, Long id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada."));
        if (!materia.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Matéria não encontrada.");
        }
        return materia;
    }

    private MateriaResponseDTO toResponseDTO(Materia materia) {
        return new MateriaResponseDTO(
                materia.getId(),
                materia.getNome(),
                materia.getDescricao(),
                materia.getCriadoEm()
        );
    }
}
