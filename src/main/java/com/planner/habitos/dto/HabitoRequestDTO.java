package com.planner.habitos.dto;

import com.planner.habitos.entity.Habito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HabitoRequestDTO(
        @NotBlank @Size(max = 200) String nome,
        String descricao,
        @NotNull Habito.Frequencia frequencia
) {}
