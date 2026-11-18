package com.planner.tarefa.dto;

import com.planner.tarefa.entity.Tarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        Tarefa.Status status,
        Tarefa.Prioridade prioridade,
        LocalDate dataVencimento,
        LocalDateTime criadoEm,
        Long projetoId
) {}
