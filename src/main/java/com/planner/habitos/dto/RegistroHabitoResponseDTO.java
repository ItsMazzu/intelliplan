package com.planner.habitos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistroHabitoResponseDTO(
        Long id,
        Long habitoId,
        LocalDate dataRegistro,
        String observacao,
        LocalDateTime registradoEm
) {}
