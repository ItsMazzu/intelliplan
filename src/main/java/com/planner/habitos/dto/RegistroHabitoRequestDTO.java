package com.planner.habitos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegistroHabitoRequestDTO(
        @NotNull Long habitoId,
        @NotNull LocalDate dataRegistro,
        String observacao
) {}
