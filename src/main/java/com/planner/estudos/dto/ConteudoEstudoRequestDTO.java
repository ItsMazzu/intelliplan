package com.planner.estudos.dto;

import com.planner.estudos.entity.ConteudoEstudo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConteudoEstudoRequestDTO(
        @NotBlank @Size(max = 300) String titulo,
        String descricao,
        @NotNull ConteudoEstudo.Status status,
        @NotNull Long materiaId
) {}
