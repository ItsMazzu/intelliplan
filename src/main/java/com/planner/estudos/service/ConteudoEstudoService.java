package com.planner.estudos.service;

import com.planner.common.exception.ResourceNotFoundException;
import com.planner.estudos.dto.ConteudoEstudoRequestDTO;
import com.planner.estudos.dto.ConteudoEstudoResponseDTO;
import com.planner.estudos.entity.ConteudoEstudo;
import com.planner.estudos.entity.Materia;
import com.planner.estudos.repository.ConteudoEstudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConteudoEstudoService {

    private final ConteudoEstudoRepository conteudoEstudoRepository;
    private final MateriaService materiaService;

    public ConteudoEstudoService(ConteudoEstudoRepository conteudoEstudoRepository,
                                 MateriaService materiaService) {
        this.conteudoEstudoRepository = conteudoEstudoRepository;
        this.materiaService = materiaService;
    }

    @Transactional
    public ConteudoEstudoResponseDTO criar(Long usuarioId, ConteudoEstudoRequestDTO dto) {
        Materia materia = materiaService.buscarMateriaDoUsuario(usuarioId, dto.materiaId());
        ConteudoEstudo conteudo = new ConteudoEstudo();
        conteudo.setMateria(materia);
        conteudo.setTitulo(dto.titulo());
        conteudo.setDescricao(dto.descricao());
        conteudo.setStatus(dto.status());
        return toResponseDTO(conteudoEstudoRepository.save(conteudo));
    }

    @Transactional(readOnly = true)
    public ConteudoEstudoResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarConteudoDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<ConteudoEstudoResponseDTO> listarPorMateria(Long usuarioId, Long materiaId) {
        materiaService.buscarMateriaDoUsuario(usuarioId, materiaId);
        return conteudoEstudoRepository.findByMateriaId(materiaId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteudoEstudoResponseDTO> listarPorMateriaEStatus(Long usuarioId, Long materiaId, ConteudoEstudo.Status status) {
        materiaService.buscarMateriaDoUsuario(usuarioId, materiaId);
        return conteudoEstudoRepository.findByMateriaIdAndStatus(materiaId, status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public ConteudoEstudoResponseDTO atualizar(Long usuarioId, Long id, ConteudoEstudoRequestDTO dto) {
        ConteudoEstudo conteudo = buscarConteudoDoUsuario(usuarioId, id);

        if (!conteudo.getMateria().getId().equals(dto.materiaId())) {
            Materia novaMateria = materiaService.buscarMateriaDoUsuario(usuarioId, dto.materiaId());
            conteudo.setMateria(novaMateria);
        }

        conteudo.setTitulo(dto.titulo());
        conteudo.setDescricao(dto.descricao());
        conteudo.setStatus(dto.status());

        return toResponseDTO(conteudoEstudoRepository.save(conteudo));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        ConteudoEstudo conteudo = buscarConteudoDoUsuario(usuarioId, id);
        conteudoEstudoRepository.delete(conteudo);
    }

    private ConteudoEstudo buscarConteudoDoUsuario(Long usuarioId, Long id) {
        ConteudoEstudo conteudo = conteudoEstudoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo de estudo não encontrado."));
        if (!conteudo.getMateria().getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Conteúdo de estudo não encontrado.");
        }
        return conteudo;
    }

    private ConteudoEstudoResponseDTO toResponseDTO(ConteudoEstudo conteudo) {
        return new ConteudoEstudoResponseDTO(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getDescricao(),
                conteudo.getStatus(),
                conteudo.getCriadoEm(),
                conteudo.getMateria().getId()
        );
    }
}
