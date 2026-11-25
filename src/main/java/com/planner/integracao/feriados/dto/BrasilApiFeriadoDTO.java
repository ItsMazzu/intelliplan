package com.planner.integracao.feriados.dto;

import java.time.LocalDate;

public record BrasilApiFeriadoDTO(
        LocalDate date,
        String name,
        String type
) {
}
