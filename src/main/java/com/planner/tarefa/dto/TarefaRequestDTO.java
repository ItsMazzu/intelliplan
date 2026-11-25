package com.planner.tarefa.dto;

import com.planner.tarefa.entity.Tarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TarefaRequestDTO(
        @NotBlank @Size(max = 300) String titulo,
        String descricao,
        @NotNull Tarefa.Status status,
        @NotNull Tarefa.Prioridade prioridade,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento,
        Long projetoId
) {}
