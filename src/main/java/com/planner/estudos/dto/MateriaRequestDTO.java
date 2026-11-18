package com.planner.estudos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MateriaRequestDTO(
        @NotBlank @Size(max = 200) String nome,
        String descricao
) {}
