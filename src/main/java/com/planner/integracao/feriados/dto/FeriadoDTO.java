package com.planner.integracao.feriados.dto;

import java.time.LocalDate;

public record FeriadoDTO(
        LocalDate date,
        String name,
        String type
) {
}
