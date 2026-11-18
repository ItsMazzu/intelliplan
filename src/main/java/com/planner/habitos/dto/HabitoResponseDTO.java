package com.planner.habitos.dto;

import com.planner.habitos.entity.Habito;

import java.time.LocalDateTime;

public record HabitoResponseDTO(
        Long id,
        String nome,
        String descricao,
        Habito.Frequencia frequencia,
        boolean ativo,
        LocalDateTime criadoEm
) {}
