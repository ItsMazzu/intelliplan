package com.planner.agenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CompromissoRequestDTO(
        @NotBlank @Size(max = 300) String titulo,
        String descricao,
        @NotNull LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        @Size(max = 300) String local
) {}
