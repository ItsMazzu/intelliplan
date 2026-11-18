package com.planner.estudos.dto;

import com.planner.estudos.entity.ConteudoEstudo;

import java.time.LocalDateTime;

public record ConteudoEstudoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        ConteudoEstudo.Status status,
        LocalDateTime criadoEm,
        Long materiaId
) {}
