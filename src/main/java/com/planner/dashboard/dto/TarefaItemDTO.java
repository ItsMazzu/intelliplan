package com.planner.dashboard.dto;

public record TarefaItemDTO(
        Long id,
        String titulo,
        boolean concluida,
        String categoria
) {
}
