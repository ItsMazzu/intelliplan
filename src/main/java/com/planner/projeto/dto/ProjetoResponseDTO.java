package com.planner.projeto.dto;

import com.planner.projeto.entity.Projeto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjetoResponseDTO(
        Long id,
        String nome,
        String descricao,
        Projeto.Status status,
        LocalDate dataInicio,
        LocalDate dataConclusao,
        LocalDateTime criadoEm
) {}
