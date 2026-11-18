package com.planner.estudos.dto;

import java.time.LocalDateTime;

public record MateriaResponseDTO(
        Long id,
        String nome,
        String descricao,
        LocalDateTime criadoEm
) {}
